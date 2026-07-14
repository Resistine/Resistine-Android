//go:build linux

package main

import (
	"bytes"
	"os"
	"testing"

	"golang.org/x/sys/unix"
	"golang.zx2c4.com/wireguard/tun"
)

func TestTelemetryDeviceMirrorsBothDirections(t *testing.T) {
	outbound, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	inboundPair, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	defer unix.Close(outbound[1])
	defer unix.Close(inboundPair[1])

	base := &fakeTunDevice{readPacket: []byte{0x45, 0x01, 0x02}}
	wrapped := newTelemetryDevice(base, outbound[0], inboundPair[0])
	defer wrapped.Close()

	readBuffer := [][]byte{make([]byte, 64)}
	sizes := make([]int, 1)
	if count, readErr := wrapped.Read(readBuffer, sizes, 4); readErr != nil || count != 1 {
		t.Fatalf("read count=%d error=%v", count, readErr)
	}
	assertTelemetryPacket(t, outbound[1], base.readPacket)

	inbound := []byte{0x60, 0x03, 0x04}
	if count, writeErr := wrapped.Write([][]byte{append([]byte{0, 0}, inbound...)}, 2); writeErr != nil || count != 1 {
		t.Fatalf("write count=%d error=%v", count, writeErr)
	}
	assertTelemetryPacket(t, inboundPair[1], inbound)

	if wrapped.droppedPackets() != 0 {
		t.Fatalf("unexpected drops: %d", wrapped.droppedPackets())
	}
}

func TestTelemetryDeviceWriteHandlesNativeTunByteCount(t *testing.T) {
	inboundPair, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	defer unix.Close(inboundPair[1])

	base := &fakeTunDevice{writeReturnsBytes: true}
	wrapped := newTelemetryDevice(base, -1, inboundPair[0])
	defer wrapped.Close()

	inbound := []byte{0x45, 0x00, 0x00, 0x14}
	buffer := append([]byte{0, 0}, inbound...)
	written, writeErr := wrapped.Write([][]byte{buffer}, 2)
	if writeErr != nil || written != len(inbound) {
		t.Fatalf("written=%d error=%v", written, writeErr)
	}
	assertTelemetryPacket(t, inboundPair[1], inbound)

	if wrapped.droppedPackets() != 0 {
		t.Fatalf("unexpected drops: %d", wrapped.droppedPackets())
	}
}

func TestTelemetryDeviceCountsClosedChannelDrop(t *testing.T) {
	outbound, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	inboundPair, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	defer unix.Close(inboundPair[1])
	base := &fakeTunDevice{readPacket: []byte{0x45, 0x01}}
	wrapped := newTelemetryDevice(base, outbound[0], inboundPair[0])
	defer wrapped.Close()
	if err := unix.Close(outbound[1]); err != nil {
		t.Fatal(err)
	}

	readBuffer := [][]byte{make([]byte, 64)}
	sizes := make([]int, 1)
	if _, err := wrapped.Read(readBuffer, sizes, 0); err != nil {
		t.Fatal(err)
	}
	if wrapped.droppedPackets() != 1 {
		t.Fatalf("drops=%d, want 1", wrapped.droppedPackets())
	}
}

func TestTelemetryDeviceDropsInsteadOfBlockingWhenChannelIsFull(t *testing.T) {
	outbound, err := unix.Socketpair(unix.AF_UNIX, unix.SOCK_SEQPACKET|unix.SOCK_CLOEXEC, 0)
	if err != nil {
		t.Fatal(err)
	}
	defer unix.Close(outbound[1])

	base := &fakeTunDevice{readPacket: bytes.Repeat([]byte{0x45}, 1500)}
	wrapped := newTelemetryDevice(base, outbound[0], -1)
	defer wrapped.Close()
	readBuffer := [][]byte{make([]byte, 2048)}
	sizes := make([]int, 1)

	for attempt := 0; attempt < 10_000 && wrapped.droppedPackets() == 0; attempt++ {
		if _, readErr := wrapped.Read(readBuffer, sizes, 0); readErr != nil {
			t.Fatal(readErr)
		}
	}
	if wrapped.droppedPackets() == 0 {
		t.Fatal("telemetry channel never filled")
	}
}

func assertTelemetryPacket(t *testing.T, fd int, packet []byte) {
	t.Helper()
	buffer := make([]byte, 128)
	count, _, err := unix.Recvfrom(fd, buffer, 0)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(buffer[:count], packet) {
		t.Fatalf("packet=%x, want %x", buffer[:count], packet)
	}
}

type fakeTunDevice struct {
	readPacket        []byte
	events            chan tun.Event
	writeReturnsBytes bool
}

func (device *fakeTunDevice) File() *os.File { return nil }

func (device *fakeTunDevice) Read(bufs [][]byte, sizes []int, offset int) (int, error) {
	copy(bufs[0][offset:], device.readPacket)
	sizes[0] = len(device.readPacket)
	return 1, nil
}

func (device *fakeTunDevice) Write(bufs [][]byte, offset int) (int, error) {
	if device.writeReturnsBytes {
		written := 0
		for _, buffer := range bufs {
			if offset >= 0 && offset < len(buffer) {
				written += len(buffer) - offset
			}
		}
		return written, nil
	}
	return len(bufs), nil
}

func (device *fakeTunDevice) MTU() (int, error) { return 1280, nil }

func (device *fakeTunDevice) Name() (string, error) { return "test", nil }

func (device *fakeTunDevice) Events() <-chan tun.Event {
	if device.events == nil {
		device.events = make(chan tun.Event)
	}
	return device.events
}

func (device *fakeTunDevice) Close() error { return nil }

func (device *fakeTunDevice) BatchSize() int { return 1 }
