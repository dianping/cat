package message

import "time"

type Transaction struct {
	Message

	DurationMs int64
}

func (t *Transaction) Complete() {
	if t.DurationMs == 0 {
		t.DurationMs = time.Now().UnixNano() - t.Message.timestampInNano
		t.DurationMs /= 1000000
	}
	t.Message.flush(t)
}

func (t *Transaction) SetDurationInMillis(durationMs int64) {
	t.DurationMs = durationMs
}
