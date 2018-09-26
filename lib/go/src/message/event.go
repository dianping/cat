package message

type Event struct {
	Message
}

func (e *Event) Complete() {
	e.Message.flush(e)
}
