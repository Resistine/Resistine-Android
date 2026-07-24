//go:build linux

package main

import (
	"errors"
	"sync"
	"sync/atomic"
	"time"

	"golang.org/x/sys/unix"
	"golang.zx2c4.com/wireguard/tun"
)

const (
	telemetryDirectionOutbound = 0
	telemetryDirectionInbound  = 1
	telemetryQueueCapacity     = 8_192
	telemetryPollTimeoutMillis = 250
	telemetryClosePollLimit    = 8
)

type telemetryDevice struct {
	tun.Device
	writers   [2]*telemetryWriter
	closeOnce sync.Once
	drops     atomic.Uint64
}

func newTelemetryDevice(device tun.Device, outboundFD int, inboundFD int) *telemetryDevice {
	return newTelemetryDeviceWithQueueCapacity(
		device,
		outboundFD,
		inboundFD,
		telemetryQueueCapacity,
	)
}

func newTelemetryDeviceWithQueueCapacity(
	device tun.Device,
	outboundFD int,
	inboundFD int,
	queueCapacity int,
) *telemetryDevice {
	wrapped := &telemetryDevice{Device: device}
	for direction, fd := range [2]int{outboundFD, inboundFD} {
		if fd >= 0 {
			wrapped.writers[direction] = newTelemetryWriter(fd, queueCapacity, &wrapped.drops)
		}
	}
	return wrapped
}

func (device *telemetryDevice) Read(bufs [][]byte, sizes []int, offset int) (int, error) {
	count, err := device.Device.Read(bufs, sizes, offset)
	for index := 0; index < count; index++ {
		length := sizes[index]
		if length <= 0 || offset < 0 || offset+length > len(bufs[index]) {
			device.drops.Add(1)
			continue
		}
		device.emit(telemetryDirectionOutbound, bufs[index][offset:offset+length])
	}
	return count, err
}

func (device *telemetryDevice) Write(bufs [][]byte, offset int) (int, error) {
	written, err := device.Device.Write(bufs, offset)
	// Linux NativeTun returns the number of bytes written even though the Device
	// contract describes a packet count. Bound the result so telemetry can safely
	// support either convention.
	telemetryCount := written
	if telemetryCount > len(bufs) {
		telemetryCount = len(bufs)
	}
	for index := 0; index < telemetryCount; index++ {
		if offset < 0 || offset >= len(bufs[index]) {
			device.drops.Add(1)
			continue
		}
		device.emit(telemetryDirectionInbound, bufs[index][offset:])
	}
	return written, err
}

func (device *telemetryDevice) Close() error {
	err := device.Device.Close()
	device.closeOnce.Do(func() {
		for _, writer := range device.writers {
			if writer != nil {
				writer.close()
			}
		}
	})
	return err
}

func (device *telemetryDevice) droppedPackets() uint64 {
	return device.drops.Load()
}

func (device *telemetryDevice) queuedPackets() uint64 {
	var total uint64
	for _, writer := range device.writers {
		if writer != nil {
			total += writer.queueDepth()
		}
	}
	return total
}

func (device *telemetryDevice) queueHighWaterMark() uint64 {
	var total uint64
	for _, writer := range device.writers {
		if writer != nil {
			total += writer.highWaterMark()
		}
	}
	return total
}

func (device *telemetryDevice) emit(direction int, packet []byte) {
	if len(packet) == 0 {
		return
	}
	if direction < 0 || direction >= len(device.writers) {
		device.drops.Add(1)
		return
	}
	writer := device.writers[direction]
	if writer == nil {
		device.drops.Add(1)
		return
	}
	writer.enqueue(packet)
}

type telemetryWriter struct {
	fd            int
	queue         chan []byte
	drops         *atomic.Uint64
	closing       atomic.Bool
	highWater     atomic.Uint64
	closeOnce     sync.Once
	queueCloseMu  sync.RWMutex
	queueClosed   bool
	workerStopped sync.WaitGroup
}

func newTelemetryWriter(fd int, queueCapacity int, drops *atomic.Uint64) *telemetryWriter {
	if queueCapacity <= 0 {
		queueCapacity = 1
	}
	writer := &telemetryWriter{
		fd:    fd,
		queue: make(chan []byte, queueCapacity),
		drops: drops,
	}
	writer.workerStopped.Add(1)
	go writer.drain()
	return writer
}

func (writer *telemetryWriter) enqueue(packet []byte) {
	copied := append([]byte(nil), packet...)
	writer.queueCloseMu.RLock()
	defer writer.queueCloseMu.RUnlock()
	if writer.queueClosed {
		writer.drops.Add(1)
		return
	}
	select {
	case writer.queue <- copied:
		writer.recordHighWater(uint64(len(writer.queue)))
	default:
		writer.drops.Add(1)
	}
}

func (writer *telemetryWriter) close() {
	writer.closeOnce.Do(func() {
		writer.closing.Store(true)
		writer.queueCloseMu.Lock()
		writer.queueClosed = true
		close(writer.queue)
		writer.queueCloseMu.Unlock()
		writer.workerStopped.Wait()
	})
}

func (writer *telemetryWriter) queueDepth() uint64 {
	return uint64(len(writer.queue))
}

func (writer *telemetryWriter) highWaterMark() uint64 {
	return writer.highWater.Load()
}

func (writer *telemetryWriter) recordHighWater(depth uint64) {
	for {
		current := writer.highWater.Load()
		if depth <= current || writer.highWater.CompareAndSwap(current, depth) {
			return
		}
	}
}

func (writer *telemetryWriter) drain() {
	defer writer.workerStopped.Done()
	defer unix.Close(writer.fd)
	for packet := range writer.queue {
		if !writer.send(packet) {
			writer.drops.Add(1)
			if writer.closing.Load() {
				for range writer.queue {
					writer.drops.Add(1)
				}
				return
			}
		}
	}
}

func (writer *telemetryWriter) send(packet []byte) bool {
	closePolls := 0
	for {
		sent, err := unix.SendmsgN(
			writer.fd,
			packet,
			nil,
			nil,
			unix.MSG_DONTWAIT|unix.MSG_NOSIGNAL,
		)
		if err == nil {
			return sent == len(packet)
		}
		if !errors.Is(err, unix.EAGAIN) && !errors.Is(err, unix.EWOULDBLOCK) {
			return false
		}

		pollFD := []unix.PollFd{{Fd: int32(writer.fd), Events: unix.POLLOUT}}
		_, pollErr := unix.Poll(pollFD, telemetryPollTimeoutMillis)
		if pollErr != nil && !errors.Is(pollErr, unix.EINTR) {
			return false
		}
		if len(pollFD) > 0 &&
			pollFD[0].Revents&(unix.POLLERR|unix.POLLHUP|unix.POLLNVAL) != 0 {
			return false
		}
		if writer.closing.Load() {
			closePolls++
			if closePolls >= telemetryClosePollLimit {
				return false
			}
		}
		if pollErr != nil {
			time.Sleep(time.Millisecond)
		}
	}
}
