package gocat

import (
	"time"

	"ccat"
	"message"
)

type catInstance struct {
}

func Instance() *catInstance {
	return &catInstance{}
}

func (t *catInstance) flush(m message.Messager) {
	ccat.Send(m)
}

func (t *catInstance) NewTransaction(mtype, name string) *message.Transaction {
	return &message.Transaction{
		Message: message.NewMessage(mtype, name, t.flush),
	}
}

func (t *catInstance) NewCompletedTransactionWithDuration(mtype, name string, durationMs int64) {
	var trans = t.NewTransaction(mtype, name)
	trans.SetDurationInMillis(durationMs)
	if durationMs > 0 && durationMs < 60*1000 {
		trans.SetTimestamp(time.Now().UnixNano()/1000000 - durationMs)
	}
	trans.SetStatus(message.SUCCESS)
	trans.Complete()
}

func (t *catInstance) NewEvent(mtype, name string) *message.Event {
	return &message.Event{
		message.NewMessage(mtype, name, t.flush),
	}
}

func (t *catInstance) NewHeartbeat(mtype, name string) *message.Heartbeat {
	return &message.Heartbeat{
		message.NewMessage(mtype, name, t.flush),
	}
}

func (t *catInstance) LogEvent(m_type, m_name string, args ...string) {
	var e = t.NewEvent(m_type, m_name)
	if len(args) > 0 {
		e.SetStatus(args[0])
	}
	if len(args) > 1 {
		e.AddData(args[1])
	}
	e.Complete()
}

func (t *catInstance) LogError(err error, args ...string) {
	var category = "error"
	if len(args) > 0 {
		category = args[0]
	}
	var e = t.NewEvent("Exception", category)
	var buf = newStacktrace(2, err)
	e.SetStatus(message.FAIL)
	e.AddData(buf.String())
	e.Complete()
}

func (t *catInstance) LogMetricForCount(m_name string, args ...int) {
	if len(args) == 0 {
		ccat.LogMetricForCount(m_name, 1)
	} else {
		ccat.LogMetricForCount(m_name, args[0])
	}
}

func (t *catInstance) LogMetricForDurationMs(m_name string, duration int64) {
	ccat.LogMetricForDurationMs(m_name, duration)
}
