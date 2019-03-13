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
package org.unidal.cat.message.storage.internals;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.unidal.cat.message.storage.Block;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;

public class DefaultBlock implements Block {

	private static final int MAX_SIZE = 256 * 1024;

	private String m_domain;

	private int m_hour;

	private ByteBuf m_data;

	private int m_offset;

	private Map<MessageId, Integer> m_offsets = new LinkedHashMap<MessageId, Integer>();

	private volatile OutputStream m_out;

	private volatile boolean m_isFlush;

	public DefaultBlock(MessageId id, int offset, byte[] data) {
		m_offsets.put(id, offset);
		m_data = data == null ? null : Unpooled.wrappedBuffer(data);
	}

	public DefaultBlock(String domain, int hour) {
		m_domain = domain;
		m_hour = hour;
		m_data = Unpooled.buffer(8 * 1024);
		m_out = createOutputSteam(m_data);
	}

	@Override
	public void clear() {
		m_data = null;
		m_offsets.clear();
	}

	private InputStream createInputSteam(ByteBuf buf) {
		ByteBufInputStream os = new ByteBufInputStream(buf);
		InputStream in = null;

		try {
			in = new SnappyInputStream(os);
		} catch (IOException e) {
			// ignore
		}
		return in;
	}

	private OutputStream createOutputSteam(ByteBuf buf) {
		ByteBufOutputStream os = new ByteBufOutputStream(buf);

		return new SnappyOutputStream(os);
	}

	@Override
	public ByteBuf find(MessageId id) {
		Integer offset = m_offsets.get(id);

		if (offset != null) {
			m_isFlush = true;

			finish();

			ByteBuf copyData = Unpooled.copiedBuffer(m_data);
			DataInputStream in = new DataInputStream(createInputSteam(copyData));

			try {
				in.skip(offset);
				int length = in.readInt();
				byte[] result = new byte[length];

				in.readFully(result);

				ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(4 + result.length);

				buf.writeInt(result.length);
				buf.writeBytes(result);

				return buf;
			} catch (IOException e) {
				Cat.logError(e);
			} finally {
				try {
					in.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

		return null;
	}

	@Override
	public synchronized void finish() {
		try {
			if (m_out != null) {
				synchronized (m_out) {
					m_out.flush();
					m_out.close();
					m_out = null;
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public ByteBuf getData() throws IOException {
		return m_data;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public int getHour() {
		return m_hour;
	}

	@Override
	public Map<MessageId, Integer> getOffsets() {
		return m_offsets;
	}

	@Override
	public boolean isFull() {
		return m_offset >= MAX_SIZE || m_isFlush;
	}

	@Override
	public void pack(MessageId id, ByteBuf buf) throws IOException {
		synchronized (m_out) {
			int len = buf.readableBytes();

			buf.readBytes(m_out, len);
			m_offsets.put(id, m_offset);
			m_offset += len;
		}
	}

	@Override
	public ByteBuf unpack(MessageId id) throws IOException {
		ByteBuf buf;

		if (m_data == null) {
			return null;
		}

		InputStream snappyIn = createInputSteam(m_data);
		Integer offset = m_offsets.get(id);

		if (snappyIn == null || offset == null) {
			return null;
		}

		DataInputStream in = new DataInputStream(snappyIn);

		in.skip(offset);

		int len = in.readInt();

		if (len < 0) {
			return null;
		}

		byte[] data = new byte[len];

		in.readFully(data);
		in.close();

		buf = UnpooledByteBufAllocator.DEFAULT.buffer(4 + data.length);
		buf.writeInt(data.length);
		buf.writeBytes(data);

		return buf;
	}
}
