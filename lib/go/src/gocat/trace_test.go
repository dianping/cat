package gocat

import (
	"runtime"
	"testing"
)

func Test_functionName(t *testing.T) {
	var skip = 0

	var names = []string{
		"gocat.Test_functionName",
		"testing.tRunner",
		"runtime.goexit",
	}
	for i := skip; ; i++ {
		if pc, _, _, ok := runtime.Caller(i); !ok {
			break
		} else if functionName(pc) != names[i] {
			t.Errorf("incorrect function name: got %s, want %s", functionName(pc), names[i])
		}
	}
}

func Test_trimPath(t *testing.T) {
	var expect = "gocat/trace_test.go"
	if _, filename, _, ok := runtime.Caller(0); !ok {
		t.Errorf("error occured in reading top of the stack")
	} else if trimPath(filename) != expect {
		t.Errorf("incorrect trimPath result: got %s, want %s", trimPath(filename), expect)
	}
}
