package gocat

import (
	"message"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_NewMessage(t *testing.T) {
	var tree = Instance()

	trans := tree.NewTransaction("foo", "bar")
	assert.Equal(t, trans.Type, "foo")
	assert.Equal(t, trans.Name, "bar")
	assert.Equal(t, trans.Status, message.SUCCESS)

	event := tree.NewEvent("foo", "bar")
	assert.Equal(t, event.Type, "foo")
	assert.Equal(t, event.Name, "bar")
	assert.Equal(t, event.Status, message.SUCCESS)
}
