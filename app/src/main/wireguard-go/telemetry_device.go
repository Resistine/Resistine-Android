//go:build linux

package main

import (
	"sync"
	"sync/atomic"

	"golang.org/x/sys/unix"
	"golang.zx2c4.com/wireguard/tun"
)

const (
	telemetryDirectionOutbound = 0
	telemetryDirectionInbound  = 1
)

type telemetryDevice struct {
	tun.Device
	fds       [2]int
	writeMu   [2]sync.Mutex
	closeOnce sync.Once
	drops     atomic.Uint64
}

func newTelemetryDevice(device tun.Device, outboundFD int, inboundFD int) *telemetryDevice {
	return &telemetryDevice{Device: device, fds: [2]int{outboundFD, inboundFD}}
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
		for direction := range device.fds {
			device.writeMu[direction].Lock()
			if device.fds[direction] >= 0 {
				_ = unix.Close(device.fds[direction])
				device.fds[direction] = -1
			}
			device.writeMu[direction].Unlock()
		}
	})
	return err
}

func (device *telemetryDevice) droppedPackets() uint64 {
	return device.drops.Load()
}

func (device *telemetryDevice) emit(direction int, packet []byte) {
	if len(packet) == 0 {
		return
	}
	if direction < 0 || direction >= len(device.fds) {
		device.drops.Add(1)
		return
	}
	device.writeMu[direction].Lock()
	defer device.writeMu[direction].Unlock()
	fd := device.fds[direction]
	if fd < 0 {
		device.drops.Add(1)
		return
	}
	sent, err := unix.SendmsgN(
		fd,
		packet,
		nil,
		nil,
		unix.MSG_DONTWAIT|unix.MSG_NOSIGNAL,
	)
	if err != nil || sent != len(packet) {
		device.drops.Add(1)
	}
}
