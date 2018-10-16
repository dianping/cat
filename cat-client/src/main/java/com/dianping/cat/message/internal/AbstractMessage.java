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
package com.dianping.cat.message.internal;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public abstract class AbstractMessage implements Message {
	protected String m_status = "unset";

	private String m_type;

	private String m_name;

	private long m_timestampInMillis;

	private CharSequence m_data;

	private boolean m_completed;

	public AbstractMessage(String type, String name) {
		m_type = String.valueOf(type);
		m_name = String.valueOf(name);
		m_timestampInMillis = MilliSecondTimer.currentTimeMillis();
	}

	@Override
	public void addData(String keyValuePairs) {
		if (m_data == null) {
			m_data = keyValuePairs;
		} else if (m_data instanceof StringBuilder) {
			((StringBuilder) m_data).append('&').append(keyValuePairs);
		} else {
			StringBuilder sb = new StringBuilder(m_data.length() + keyValuePairs.length() + 16);

			sb.append(m_data).append('&');
			sb.append(keyValuePairs);
			m_data = sb;
		}
	}

	@Override
	public void addData(String key, Object value) {
		if (m_data instanceof StringBuilder) {
			((StringBuilder) m_data).append('&').append(key).append('=').append(value);
		} else {
			String str = String.valueOf(value);
			int old = m_data == null ? 0 : m_data.length();
			StringBuilder sb = new StringBuilder(old + key.length() + str.length() + 16);

			if (m_data != null) {
				sb.append(m_data).append('&');
			}

			sb.append(key).append('=').append(str);
			m_data = sb;
		}
	}

	@Override
	public CharSequence getData() {
		if (m_data == null) {
			return "";
		} else {
			return m_data;
		}
	}

	public void setData(String str) {
		m_data = str;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public String getStatus() {
		return m_status;
	}

	@Override
	public void setStatus(Throwable e) {
		m_status = e.getClass().getName();
	}

	@Override
	public long getTimestamp() {
		return m_timestampInMillis;
	}

	@Override
	public void setTimestamp(long timestamp) {
		m_timestampInMillis = timestamp;
	}

	@Override
	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public boolean isCompleted() {
		return m_completed;
	}

	public void setCompleted(boolean completed) {
		m_completed = completed;
	}

	@Override
	public boolean isSuccess() {
		return Message.SUCCESS.equals(m_status);
	}

	@Override
	public void setStatus(String status) {
		m_status = status;
	}

	@Override
	public String toString() {
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

		codec.encodeMessage(this, buf);
		codec.reset();
		return buf.toString(Charset.forName("utf-8"));
	}

	@Override
	public void setSuccessStatus() {
		m_status = SUCCESS;
	}

}
