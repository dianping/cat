package test

import (
	"errors"
	"testing"
	"time"

	"gocat"
)

var cat = gocat.Instance()

const TTYPE = "foo"

func init() {
	gocat.Init("gocat")
}

// send transaction
func case1() {
	t := cat.NewTransaction(TTYPE, "test")
	defer t.Complete()
	t.AddData("testcase")
	t.AddData("foo", "bar")
	t.SetStatus(gocat.FAIL)
	t.SetTimestamp(time.Now().UnixNano()/1000/1000 - 20*1000)
	t.SetDurationInMillis(15 * 1000)
}

// send completed transaction with duration
func case2() {
	cat.NewCompletedTransactionWithDuration(TTYPE, "completed", 24000)
	cat.NewCompletedTransactionWithDuration(TTYPE, "completed-over-60s", 65000)
}

// send event
func case3() {
	// way 1
	e := cat.NewEvent(TTYPE, "event-1")
	e.Complete()
	// way 2
	cat.LogEvent(TTYPE, "event-2")
	cat.LogEvent(TTYPE, "event-3", gocat.FAIL)
	cat.LogEvent(TTYPE, "event-4", gocat.FAIL, "foobar")
}

// send error with backtrace
func case4() {
	err := errors.New("error")
	cat.LogError(err)
}

// send metric
func case5() {
	cat.LogMetricForCount("metric-1")
	cat.LogMetricForCount("metric-2", 3)
	cat.LogMetricForDurationMs("metric-3", 150)
}

func run(f func()) {
	for {
		f()
	}
}

func Test_Message_Case(t *testing.T) {
	go run(case1)
	go run(case2)
	go run(case3)
	go run(case4)
	go run(case5)

	// wait until main process has been killed
	var ch chan int
	<-ch
}
