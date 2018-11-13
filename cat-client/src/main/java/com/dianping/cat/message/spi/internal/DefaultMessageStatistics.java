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
package com.dianping.cat.message.spi.internal;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageStatistics.class)
public class DefaultMessageStatistics implements MessageStatistics {
	private long m_produced;

	private long m_overflowed;

	private long m_bytes;

	@Override
	public long getBytes() {
		return m_bytes;
	}

	@Override
	public long getOverflowed() {
		return m_overflowed;
	}

	@Override
	public long getProduced() {
		return m_produced;
	}

	@Override
	public void onBytes(int bytes) {
		m_bytes += bytes;
		m_produced++;
	}

	@Override
	public void onOverflowed(MessageTree tree) {
		m_overflowed++;
	}
}
