//go:build android

package main

// #cgo LDFLAGS: -llog
// #include <android/log.h>
import "C"

import (
	"fmt"
	"math"
	"net"
	"os"
	"os/signal"
	"runtime"
	"runtime/debug"
	"strings"
	"sync"
	"unsafe"

	"golang.org/x/sys/unix"
	"golang.zx2c4.com/wireguard/conn"
	"golang.zx2c4.com/wireguard/device"
	"golang.zx2c4.com/wireguard/ipc"
	"golang.zx2c4.com/wireguard/tun"
)

type androidLogger struct {
	level C.int
	tag   *C.char
}

func cstring(value string) *C.char {
	bytes, err := unix.BytePtrFromString(value)
	if err != nil {
		empty := [1]C.char{}
		return &empty[0]
	}
	return (*C.char)(unsafe.Pointer(bytes))
}

func (logger androidLogger) Printf(format string, args ...interface{}) {
	C.__android_log_write(logger.level, logger.tag, cstring(fmt.Sprintf(format, args...)))
}

type tunnelHandle struct {
	device    *device.Device
	uapi      net.Listener
	telemetry *telemetryDevice
}

var (
	handlesMu sync.RWMutex
	handles   = make(map[int32]tunnelHandle)
)

func init() {
	signals := make(chan os.Signal)
	signal.Notify(signals, unix.SIGUSR2)
	go func() {
		buffer := make([]byte, os.Getpagesize())
		for range signals {
			count := runtime.Stack(buffer, true)
			if count == len(buffer) {
				count--
			}
			buffer[count] = 0
			C.__android_log_write(
				C.ANDROID_LOG_ERROR,
				cstring("Resistine/WireGuard/Stacktrace"),
				(*C.char)(unsafe.Pointer(&buffer[0])),
			)
		}
	}()
}

//export resistineWgTurnOn
func resistineWgTurnOn(
	interfaceName string,
	tunFD int32,
	outboundTelemetryFD int32,
	inboundTelemetryFD int32,
	settings string,
) int32 {
	tag := cstring("Resistine/WireGuard/" + interfaceName)
	logger := &device.Logger{
		Verbosef: androidLogger{level: C.ANDROID_LOG_DEBUG, tag: tag}.Printf,
		Errorf:   androidLogger{level: C.ANDROID_LOG_ERROR, tag: tag}.Printf,
	}

	nativeTun, name, err := tun.CreateUnmonitoredTUNFromFD(int(tunFD))
	if err != nil {
		_ = unix.Close(int(tunFD))
		closeTelemetryFD(outboundTelemetryFD)
		closeTelemetryFD(inboundTelemetryFD)
		logger.Errorf("CreateUnmonitoredTUNFromFD: %v", err)
		return -1
	}

	var telemetry *telemetryDevice
	var tunnelDevice tun.Device = nativeTun
	outboundFD := prepareTelemetryFD(outboundTelemetryFD, "outbound", logger)
	inboundFD := prepareTelemetryFD(inboundTelemetryFD, "inbound", logger)
	if outboundFD >= 0 || inboundFD >= 0 {
		telemetry = newTelemetryDevice(nativeTun, outboundFD, inboundFD)
		tunnelDevice = telemetry
	}

	logger.Verbosef("Attaching to interface %v", name)
	wireGuardDevice := device.NewDevice(tunnelDevice, conn.NewStdNetBind(), logger)
	if err := wireGuardDevice.IpcSet(settings); err != nil {
		wireGuardDevice.Close()
		logger.Errorf("IpcSet: %v", err)
		return -1
	}
	wireGuardDevice.DisableSomeRoamingForBrokenMobileSemantics()

	var uapi net.Listener
	uapiFile, err := ipc.UAPIOpen(name)
	if err != nil {
		logger.Errorf("UAPIOpen: %v", err)
	} else {
		uapi, err = ipc.UAPIListen(name, uapiFile)
		if err != nil {
			_ = uapiFile.Close()
			logger.Errorf("UAPIListen: %v", err)
		} else {
			go func() {
				for {
					connection, acceptErr := uapi.Accept()
					if acceptErr != nil {
						return
					}
					go wireGuardDevice.IpcHandle(connection)
				}
			}()
		}
	}

	if err := wireGuardDevice.Up(); err != nil {
		logger.Errorf("Unable to bring up device: %v", err)
		if uapi != nil {
			_ = uapi.Close()
		}
		wireGuardDevice.Close()
		return -1
	}
	logger.Verbosef("Device started")

	handlesMu.Lock()
	defer handlesMu.Unlock()
	var handle int32
	for handle = 0; handle < math.MaxInt32; handle++ {
		if _, exists := handles[handle]; !exists {
			break
		}
	}
	if handle == math.MaxInt32 {
		logger.Errorf("Unable to find empty handle")
		if uapi != nil {
			_ = uapi.Close()
		}
		wireGuardDevice.Close()
		return -1
	}
	handles[handle] = tunnelHandle{
		device:    wireGuardDevice,
		uapi:      uapi,
		telemetry: telemetry,
	}
	return handle
}

//export resistineWgTurnOff
func resistineWgTurnOff(handle int32) uint64 {
	handlesMu.Lock()
	tunnel, ok := handles[handle]
	if ok {
		delete(handles, handle)
	}
	handlesMu.Unlock()
	if !ok {
		return 0
	}
	if tunnel.uapi != nil {
		_ = tunnel.uapi.Close()
	}
	tunnel.device.Close()
	if tunnel.telemetry == nil {
		return 0
	}
	return tunnel.telemetry.droppedPackets()
}

//export resistineWgGetSocketV4
func resistineWgGetSocketV4(handle int32) int32 {
	tunnel, ok := lookupHandle(handle)
	if !ok {
		return -1
	}
	bind, _ := tunnel.device.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd4()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export resistineWgGetSocketV6
func resistineWgGetSocketV6(handle int32) int32 {
	tunnel, ok := lookupHandle(handle)
	if !ok {
		return -1
	}
	bind, _ := tunnel.device.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd6()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export resistineWgGetConfig
func resistineWgGetConfig(handle int32) *C.char {
	tunnel, ok := lookupHandle(handle)
	if !ok {
		return nil
	}
	settings, err := tunnel.device.IpcGet()
	if err != nil {
		return nil
	}
	return C.CString(settings)
}

//export resistineWgGetTelemetryDrops
func resistineWgGetTelemetryDrops(handle int32) uint64 {
	tunnel, ok := lookupHandle(handle)
	if !ok || tunnel.telemetry == nil {
		return 0
	}
	return tunnel.telemetry.droppedPackets()
}

//export resistineWgGetTelemetryQueueDepth
func resistineWgGetTelemetryQueueDepth(handle int32) uint64 {
	tunnel, ok := lookupHandle(handle)
	if !ok || tunnel.telemetry == nil {
		return 0
	}
	return tunnel.telemetry.queuedPackets()
}

//export resistineWgGetTelemetryQueueHighWater
func resistineWgGetTelemetryQueueHighWater(handle int32) uint64 {
	tunnel, ok := lookupHandle(handle)
	if !ok || tunnel.telemetry == nil {
		return 0
	}
	return tunnel.telemetry.queueHighWaterMark()
}

//export resistineWgVersion
func resistineWgVersion() *C.char {
	info, ok := debug.ReadBuildInfo()
	if !ok {
		return C.CString("unknown")
	}
	for _, dependency := range info.Deps {
		if dependency.Path == "golang.zx2c4.com/wireguard" {
			parts := strings.Split(dependency.Version, "-")
			if len(parts) == 3 && len(parts[2]) == 12 {
				return C.CString(parts[2][:7])
			}
			return C.CString(dependency.Version)
		}
	}
	return C.CString("unknown")
}

func lookupHandle(handle int32) (tunnelHandle, bool) {
	handlesMu.RLock()
	defer handlesMu.RUnlock()
	tunnel, ok := handles[handle]
	return tunnel, ok
}

func closeTelemetryFD(fd int32) {
	if fd >= 0 {
		_ = unix.Close(int(fd))
	}
}

func prepareTelemetryFD(fd int32, direction string, logger *device.Logger) int {
	if fd < 0 {
		return -1
	}
	if err := unix.SetNonblock(int(fd), true); err != nil {
		logger.Errorf("%s telemetry channel disabled: %v", direction, err)
		closeTelemetryFD(fd)
		return -1
	}
	return int(fd)
}

func main() {}
