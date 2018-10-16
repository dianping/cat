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
package com.dianping.cat.message.storage;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.unidal.lookup.annotation.Named;
import org.xerial.snappy.SnappyOutputStream;

import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageBucket.class, value = LocalMessageBucket.ID, instantiationStrategy = Named.PER_LOOKUP)
public class LocalMessageBucket implements MessageBucket {
	public static final String ID = "local";

	private static final int MAX_BLOCK_SIZE = 1 << 16; // 64K

	private File m_baseDir = new File(".");

	private MessageBlockWriter m_writer;

	private AtomicBoolean m_dirty = new AtomicBoolean();

	private String m_dataFile;

	private long m_lastAccessTime;

	private OutputStream m_out;

	private ByteArrayOutputStream m_buf;

	private MessageBlock m_block;

	private int m_blockSize;

	@Override
	public void close() throws IOException {
		synchronized (this) {
			if (m_writer != null) {
				m_writer.close();
				m_out.close();
				m_buf.close();
				m_out = null;
				m_buf = null;
				m_writer = null;
			}
		}
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		return findByIndex(index);
	}

	public MessageTree findByIndex(int index) throws IOException {
		File file = new File(m_baseDir, m_dataFile);
		MessageBlockReader reader = new MessageBlockReader(file);

		try {
			m_lastAccessTime = System.currentTimeMillis();

			byte[] data = reader.readMessage(index);

			if (data != null) {
				ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(data.length);

				buf.writeBytes(data);
				return CodecHandler.decode(buf);
			} else {
				return null;
			}
		} catch (EOFException e) {
			return null;
		} finally {
			reader.close();
			CodecHandler.reset();
		}
	}

	public MessageBlock flushBlock() throws IOException {
		if (m_dirty.get()) {
			synchronized (this) {
				m_out.close();
				byte[] data = m_buf.toByteArray();

				try {
					m_block.setData(data);
					m_blockSize = 0;
					m_buf.reset();
					m_out = new SnappyOutputStream(m_buf);
					m_dirty.set(false);

					return m_block;
				} finally {
					m_block = new MessageBlock(m_dataFile);
				}
			}
		}
		return null;
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	public MessageBlockWriter getWriter() {
		return m_writer;
	}

	@Override
	public void initialize(String dataFile) throws IOException {
		m_dataFile = dataFile;

		File file = new File(m_baseDir, dataFile);

		m_writer = new MessageBlockWriter(file);
		m_block = new MessageBlock(m_dataFile);
		m_buf = new ByteArrayOutputStream(16384);
		m_out = new SnappyOutputStream(m_buf);
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	public MessageBlock storeMessage(final ByteBuf buf, final MessageId id) throws IOException {
		synchronized (this) {
			try {
				int size = buf.readableBytes();

				m_dirty.set(true);
				m_lastAccessTime = System.currentTimeMillis();
				m_blockSize += size;
				m_block.addIndex(id.getIndex(), size);
				buf.getBytes(0, m_out, size); // write buffer and compress it

				if (m_blockSize >= MAX_BLOCK_SIZE) {
					return flushBlock();
				} else {
					return null;
				}
			} finally {
				// buf.release();
			}
		}
	}

	@Override
	public void initialize(String dataFile, Date date) throws IOException {
		initialize(dataFile);
	}

}
