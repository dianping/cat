package ccat

/*
#cgo CFLAGS: -I./include
#cgo darwin LDFLAGS: -L${SRCDIR}/lib/darwin/ -lcatclient -Wl,-rpath,./darwin
#cgo !darwin LDFLAGS: -L${SRCDIR}/lib/linux/ -lcatclient -lm -Wl,-rpath,./linux
#include <stdlib.h>
#include "ccat.h"
*/
import "C"
import (
	"runtime"
	"sync"
	"unsafe"
)

var ch = make(chan interface{}, 128)
var wg sync.WaitGroup

func Init(domain string) {
	var c_domain = C.CString(domain)
	defer C.free(unsafe.Pointer(c_domain))
	C.catClientInit(c_domain)
}

func BuildConfig(
	encoderType,
	enableHeartbeat,
	enableSampling,
	enableDebugLog int,
) C.CatClientConfig {
	return C.CatClientConfig{
		C.int(encoderType),
		C.int(enableHeartbeat),
		C.int(enableSampling),
		0,
		C.int(enableDebugLog),
	}
}

func InitWithConfig(domain string,  _config C.CatClientConfig) {
	var _domain = C.CString(domain)
	defer C.free(unsafe.Pointer(_domain))
	C.catClientInitWithConfig(_domain, &_config)
}

func Background() {
	// We need running ccat functions on the same thread due to ccat is using a thread local.
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	wg.Add(1)
	defer wg.Done()

	for m := range ch {
		switch m := m.(type) {
		case *Transaction:
			LogTransaction(m)
		case *Event:
			LogEvent(m)
		}
	}
}

func Shutdown() {
	close(ch)
}

func Wait() {
	wg.Wait()
}

func ShutdownAndWait() {
	Shutdown()
	Wait()
}

func Send(m Messager) {
	ch <- m
}

func LogTransaction(trans *Transaction) {
	var (
		ctype   = C.CString(trans.Type)
		cname   = C.CString(trans.Name)
		cstatus = C.CString(trans.Status)
		cdata   = C.CString(trans.GetData().String())
	)
	defer C.free(unsafe.Pointer(ctype))
	defer C.free(unsafe.Pointer(cname))
	defer C.free(unsafe.Pointer(cstatus))
	defer C.free(unsafe.Pointer(cdata))
	C.callLogTransaction(
		ctype,
		cname,
		cstatus,
		cdata,
		C.ulonglong(trans.GetTimestamp()/1000/1000),
		C.ulonglong(trans.GetTimestamp()/1000/1000),
		C.ulonglong(trans.GetDuration()/1000/1000),
	)
}

func LogEvent(event *Event) {
	var (
		ctype   = C.CString(event.Type)
		cname   = C.CString(event.Name)
		cstatus = C.CString(event.Status)
		cdata   = C.CString(event.GetData().String())
	)
	defer C.free(unsafe.Pointer(ctype))
	defer C.free(unsafe.Pointer(cname))
	defer C.free(unsafe.Pointer(cstatus))
	defer C.free(unsafe.Pointer(cdata))

	C.callLogEvent(
		ctype,
		cname,
		cstatus,
		cdata,
		C.ulonglong(event.GetTimestamp()/1000/1000),
	)
}

func LogMetricForCount(name string, count int) {
	var cname = C.CString(name)
	defer C.free(unsafe.Pointer(cname))
	C.logMetricForCount(
		cname,
		C.int(count),
	)
}

func LogMetricForDuration(name string, durationInNano int64) {
	var cname = C.CString(name)
	defer C.free(unsafe.Pointer(cname))
	C.logMetricForDuration(
		cname,
		C.ulonglong(durationInNano/1000/1000),
	)
}
