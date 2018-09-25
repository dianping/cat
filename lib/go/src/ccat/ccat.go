package ccat

/*
#cgo CFLAGS: -I./include
#cgo darwin LDFLAGS: -L${SRCDIR}/darwin/ -lcatclientstatic -Wl,-rpath,./darwin
#cgo !darwin LDFLAGS: -L${SRCDIR}/linux/ -lcatclientstatic -lm -Wl,-rpath,./linux
#include <stdlib.h>
#include "ccat.h"
*/
import "C"
import (
	"runtime"
	"sync"
	"unsafe"

	"message"
)

var ch = make(chan interface{}, 128)
var wg sync.WaitGroup

func Init(domain string) {
	var (
		c_language         = C.CString("golang")
		c_language_version = C.CString(runtime.Version())
		c_domain           = C.CString(domain)
	)
	defer C.free(unsafe.Pointer(c_language))
	defer C.free(unsafe.Pointer(c_language_version))
	defer C.free(unsafe.Pointer(c_domain))
	C.catSetLanguageBinding(c_language, c_language_version)
	C.catClientInit(c_domain)
}

func Background() {
	// We need running ccat functions on the same thread due to ccat is using a thread local.
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	wg.Add(1)
	defer wg.Done()

	for m := range ch {
		switch m := m.(type) {
		case *message.Transaction:
			LogTransaction(m)
		case *message.Event:
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

func Send(m message.Messager) {
	ch <- m
}

func LogTransaction(trans *message.Transaction) {
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
		C.ulonglong(trans.GetTimestamp()),
		C.ulonglong(trans.GetTimestamp()),
		C.ulonglong(trans.DurationMs),
	)
}

func LogEvent(event *message.Event) {
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
	C.logEventWithTime(
		ctype,
		cname,
		cstatus,
		cdata,
		C.ulonglong(event.GetTimestamp()),
	)
}

func LogBatchTransaction(m_type, m_name string, m_count, m_error, m_sum int) {
	var (
		ctype = C.CString(m_type)
		cname = C.CString(m_name)
	)
	defer C.free(unsafe.Pointer(ctype))
	defer C.free(unsafe.Pointer(cname))
	C.logBatchTransaction(
		ctype,
		cname,
		C.int(m_count),
		C.int(m_error),
		C.ulonglong(m_sum),
	)
}

func LogBatchEvent(m_type, m_name string, m_count, m_error int) {
	var (
		ctype = C.CString(m_type)
		cname = C.CString(m_name)
	)
	defer C.free(unsafe.Pointer(ctype))
	defer C.free(unsafe.Pointer(cname))
	C.logBatchEvent(
		ctype,
		cname,
		C.int(m_count),
		C.int(m_error),
	)
}

func LogMetricForCount(m_name string, m_count int) {
	var cname = C.CString(m_name)
	defer C.free(unsafe.Pointer(cname))
	C.logMetricForCountQuantity(
		cname,
		C.int(m_count),
	)
}

func LogMetricForDurationMs(m_name string, duration int64) {
	var cname = C.CString(m_name)
	defer C.free(unsafe.Pointer(cname))
	C.logMetricForDuration(
		cname,
		C.ulonglong(duration),
	)
}

func LogMetricForCountWithTags(m_name string, count int64, tags map[string]string) {
	var (
		cname  = C.CString(m_name)
		helper = C.CatBuildMetricHelper(cname)
	)
	defer C.free(unsafe.Pointer(cname))
	// defer C.free(unsafe.Pointer(helper))

	for key, val := range tags {
		var (
			ckey = C.CString(key)
			cval = C.CString(val)
		)
		C.callAddTag(helper, ckey, cval)
		C.free(unsafe.Pointer(ckey))
		C.free(unsafe.Pointer(cval))
	}
	C.callCount(helper, C.int(count))
}

func LogMetricForDurationMsWithTags(m_name string, duration int64, tags map[string]string) {
	var (
		cname  = C.CString(m_name)
		helper = C.CatBuildMetricHelper(cname)
	)
	defer C.free(unsafe.Pointer(cname))
	// defer C.free(unsafe.Pointer(helper))

	for key, val := range tags {
		var (
			ckey = C.CString(key)
			cval = C.CString(val)
		)
		C.callAddTag(helper, ckey, cval)
		C.free(unsafe.Pointer(ckey))
		C.free(unsafe.Pointer(cval))
	}
	C.callDuration(helper, C.int(duration))
}
