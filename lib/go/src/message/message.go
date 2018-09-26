package message

import (
	"bytes"
	"time"
)

const (
	SUCCESS = "0"
	FAIL = "-1"
)

type Flush func(m Messager)

type MessageGetter interface {
	GetData() *bytes.Buffer
	GetTime() time.Time
}

type Messager interface {
	MessageGetter
	AddData(k string, v ...string)
	SetStatus(status string)
	Complete()
}

type Message struct {
	Type   string
	Name   string
	Status string

	timestampInNano int64

	m_data *bytes.Buffer

	flush Flush
}

func NewMessage(mtype, name string, flush Flush) Message {
	return Message{
		Type:            mtype,
		Name:            name,
		Status:          SUCCESS,
		timestampInNano: time.Now().UnixNano(),
		m_data:          new(bytes.Buffer),
		flush:           flush,
	}
}

func (m *Message) Complete() {
	m.flush(m)
}

func (m *Message) GetData() *bytes.Buffer {
	return m.m_data
}

func (m *Message) GetTime() time.Time {
	return time.Unix(0, m.timestampInNano)
}

func (t *Message) SetTimestamp(timestampMs int64) {
	t.timestampInNano = timestampMs * 1000000
}

func (m *Message) GetTimestamp() int64 {
	return m.timestampInNano / 1000000
}

func (m *Message) AddData(k string, v ...string) {
	if m.m_data.Len() != 0 {
		m.m_data.WriteRune('&')
	}
	if len(v) == 0 {
		m.m_data.WriteString(k)
	} else {
		m.m_data.WriteString(k)
		m.m_data.WriteRune('=')
		m.m_data.WriteString(v[0])
	}
}

func (m *Message) SetStatus(status string) {
	m.Status = status
}
