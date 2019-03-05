/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
