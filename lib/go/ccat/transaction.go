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
