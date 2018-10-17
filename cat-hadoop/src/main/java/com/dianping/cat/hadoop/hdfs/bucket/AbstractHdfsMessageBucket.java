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
package com.dianping.cat.hadoop.hdfs.bucket;

import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.MessageBlockReader;
import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucket;

public abstract class AbstractHdfsMessageBucket implements MessageBucket {

	@Inject
	protected FileSystemManager m_manager;

	protected MessageBlockReader m_reader;

	protected long m_lastAccessTime;

	protected String m_id = ServerConfigManager.DUMP_DIR;

	@Override
	public void close() throws IOException {
		m_reader.close();
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		try {
			byte[] data = m_reader.readMessage(index);

			if (data != null) {
				ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(data.length);

				buf.writeBytes(data);

				MessageTree tree = CodecHandler.decode(buf);

				m_lastAccessTime = System.currentTimeMillis();
				return tree;
			} else {
				return null;
			}
		} catch (EOFException e) {
			return null;
		} finally {
			CodecHandler.reset();
		}
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	public abstract void initialize(String dataFile) throws IOException;

	public abstract void initialize(String dataFile, Date date) throws IOException;

}
