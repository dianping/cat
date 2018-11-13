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
package org.unidal.cat.message.storage.local;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;

/**
	* Supports up to 64K tokens mapping from <code>String</code> to <code>int</code>, or reverse by local file system.
	*/
@Named(type = TokenMapping.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalTokenMapping implements TokenMapping {
	private static final int BLOCK_SIZE = 32 * 1024;

	private static final String MAGIC_CODE = "TokenMapping"; // token mapping

	@Inject("local")
	private PathBuilder m_bulider;

	private RandomAccessFile m_file;

	private File m_path;

	private List<String> m_tokens = new ArrayList<String>(1024);

	private Map<String, Integer> m_map = new HashMap<String, Integer>(1024);

	private int m_block;

	private ByteBuf m_data;

	private long m_lastAccessTime;

	private boolean m_dirty;

	@Override
	public void close() {
		try {
			flush();
		} catch (IOException e) {
			Cat.logError(e);
		}

		try {
			m_file.close();
		} catch (IOException e) {
			Cat.logError(e);
		}

		m_tokens.clear();
		m_map.clear();
	}

	@Override
	public String find(int index) throws IOException {
		int len = m_tokens.size();

		if (index < len) {
			m_lastAccessTime = System.currentTimeMillis();

			return m_tokens.get(index);
		} else {
			return null;
		}
	}

	private void flush() throws IOException {
		if (m_dirty) {
			m_file.seek(1L * m_block * BLOCK_SIZE);
			m_file.write(m_data.array());
			m_dirty = false;
		}
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	private void loadFrom(ByteBuf buf, int length) throws IOException {
		int index = m_map.size();

		buf.writerIndex(length);

		while (buf.isReadable(2)) {
			short len = buf.readShort();

			if (len <= 0) {
				break;
			}

			byte[] data = new byte[len];

			buf.readBytes(data, 0, len);

			String token = new String(data, 0, len, "utf-8");

			m_tokens.add(token);
			m_map.put(token, index++);
		}

		buf.writerIndex(buf.readerIndex());
	}

	@Override
	public int map(String token) throws IOException {
		Integer index = m_map.get(token);

		if (index == null) {
			synchronized (m_map) {
				index = m_map.get(token);

				if (index == null) {
					byte[] ba = token.getBytes("utf-8");
					int len = ba.length;

					if (!m_data.isWritable(2 + len)) { // no enough space
						flush();
						m_data.clear();
						m_data.setZero(0, m_data.capacity());
						m_block++;
					}

					index = m_tokens.size();

					m_data.writeShort(len);
					m_data.writeBytes(ba);
					m_tokens.add(token);
					m_map.put(token, index);
					m_dirty = true;
					m_lastAccessTime = System.currentTimeMillis();
				}
			}
		}
		return index.intValue();
	}

	@Override
	public void open(int hour, String ip) throws IOException {
		m_path = new File(m_bulider.getPath(null, new Date(hour * TimeHelper.ONE_HOUR), ip, FileType.TOKEN));
		m_path.getParentFile().mkdirs();
		m_file = new RandomAccessFile(m_path, "rwd"); // read-write without meta sync
		m_data = Unpooled.buffer(BLOCK_SIZE);
		m_block = 0;

		while (true) {
			m_file.seek(1L * m_block * BLOCK_SIZE);

			int size = m_file.read(m_data.array());

			if (size < 0) {
				break;
			}

			loadFrom(m_data, size);
			m_data.clear();
			m_data.setZero(0, m_data.capacity());
			m_block++;
		}

		if (m_map.isEmpty()) {
			m_data.writeShort(MAGIC_CODE.length());
			m_data.writeBytes(MAGIC_CODE.getBytes());
			m_tokens.add(MAGIC_CODE);
			m_map.put(MAGIC_CODE, 0);
		}
	}
}
