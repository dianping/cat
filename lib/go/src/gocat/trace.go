package gocat

import (
	"bytes"
	"fmt"
	"go/build"
	"path/filepath"
	"runtime"
	"strings"
)

var trimPaths []string

func init() {
	for _, prefix := range build.Default.SrcDirs() {
		if prefix[len(prefix)-1] != filepath.Separator {
			prefix += string(filepath.Separator)
		}
		trimPaths = append(trimPaths, prefix)
	}
}

func trimPath(filename string) string {
	for _, prefix := range trimPaths {
		if trimmed := strings.TrimPrefix(filename, prefix); len(trimmed) < len(filename) {
			return trimmed
		}
	}
	return filename
}

func functionName(pc uintptr) string {
	fn := runtime.FuncForPC(pc)
	if fn == nil {
		return "unknown"
	}
	return fn.Name()
}

func newStacktrace(skip int, err error) (buf *bytes.Buffer) {
	buf = bytes.NewBuffer([]byte{})
	buf.WriteString(err.Error())
	buf.WriteRune('\n')
	for i := skip; ; i++ {
		pc, file, line, ok := runtime.Caller(i)
		if !ok {
			break
		}
		file = trimPath(file)
		name := functionName(pc)
		// `runtime.goexit` is bootstrap loader and is meaningless in tracing.
		if name == "runtime.goexit" {
			continue
		}
		buf.WriteString(fmt.Sprintf("at %s(%s:%d)\n", name, file, line))
	}
	return buf
}
