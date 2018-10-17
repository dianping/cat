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
package ccat

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

	data *bytes.Buffer

	flush Flush
}

func NewMessage(mtype, name string, flush Flush) *Message {
	return &Message{
		Type:            mtype,
		Name:            name,
		Status:          SUCCESS,
		timestampInNano: time.Now().UnixNano(),
		data:            new(bytes.Buffer),
		flush:           flush,
	}
}

func (m *Message) Complete() {
	m.flush(m)
}

func (m *Message) GetData() *bytes.Buffer {
	return m.data
}

func (m *Message) GetTime() time.Time {
	return time.Unix(0, m.timestampInNano)
}

func (m *Message) SetTimestamp(timestampInNano int64) {
	m.timestampInNano = timestampInNano
}

func (m *Message) GetTimestamp() int64 {
	return m.timestampInNano
}

func (m *Message) AddData(k string, v ...string) {
	if m.data.Len() != 0 {
		m.data.WriteRune('&')
	}
	if len(v) == 0 {
		m.data.WriteString(k)
	} else {
		m.data.WriteString(k)
		m.data.WriteRune('=')
		m.data.WriteString(v[0])
	}
}

func (m *Message) SetStatus(status string) {
	m.Status = status
}
