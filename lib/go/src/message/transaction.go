package message

import (
	"time"
)

type Transaction struct {
	Message

	durationInNano      int64
	durationStartInNano int64
}

func NewTransaction(mtype, name string, flush Flush) *Transaction {
	return &Transaction{
		Message:             *NewMessage(mtype, name, flush),
		durationStartInNano: time.Now().UnixNano(),
	}
}

func (t *Transaction) Complete() {
	if t.durationInNano == 0 {
		durationNano := time.Now().UnixNano() - t.durationStartInNano
		t.durationInNano = durationNano
	}
	t.Message.flush(t)
}

func (t *Transaction) GetDuration() int64 {
	return t.durationInNano
}

func (t *Transaction) SetDuration(durationInNano int64) {
	t.durationInNano = durationInNano
}

func (t *Transaction) SetDurationStart(durationStartInNano int64) {
	t.durationStartInNano = durationStartInNano
}
