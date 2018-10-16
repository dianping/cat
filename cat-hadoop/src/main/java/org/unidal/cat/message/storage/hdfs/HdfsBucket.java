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
package org.unidal.cat.message.storage.hdfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Bucket.class, value = HdfsBucket.ID, instantiationStrategy = Named.PER_LOOKUP)
public class HdfsBucket implements Bucket {
	public static final String ID = "hdfs";

	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject
	protected HdfsSystemManager m_manager;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject("hdfs")
	private PathBuilder m_bulider;

	private DataHelper m_data = new DataHelper();

	private IndexHelper m_index = new IndexHelper();

	private long m_lastAccessTime;

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_index.close();
			m_data.close();
		}
	}

	public void flush() {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public ByteBuf get(MessageId id) throws IOException {
		m_lastAccessTime = System.currentTimeMillis();
		long address = m_index.read(id);

		if (address < 0) {
			return null;
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;
			byte[] data = m_data.read(dataOffset);
			DefaultBlock block = new DefaultBlock(id, segmentOffset, data);

			return block.unpack(id);
		}
	}

	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	public boolean initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		FileSystem fs = m_manager.getFileSystem();
		String dataPath = m_bulider.getPath(domain, startTime, ip, FileType.DATA);
		String indexPath = m_bulider.getPath(domain, startTime, ip, FileType.INDEX);

		final Path hdfsDataPath = new Path(dataPath);
		final Path hdfsIndexPath = new Path(indexPath);

		if (fs.exists(hdfsDataPath) && fs.exists(hdfsIndexPath)) {
			FSDataInputStream dataStream = fs.open(new Path(dataPath));
			FSDataInputStream indexStream = fs.open(new Path(indexPath));

			m_data.init(dataStream);
			m_index.init(indexStream);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void puts(ByteBuf data, Map<MessageId, Integer> mappings) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public boolean initialize(String domain, String ip, int hour, boolean writeMode) throws IOException {
		return initialize(domain, ip, hour);
	}

	private class DataHelper {

		private FSDataInputStream m_dataStream;

		public void close() {
			try {
				m_dataStream.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
		}

		public void init(FSDataInputStream dataStream) throws IOException {
			m_dataStream = dataStream;
		}

		public byte[] read(long dataOffset) throws IOException {
			m_dataStream.seek(dataOffset);

			int len = m_dataStream.readInt();
			byte[] data = new byte[len];

			m_dataStream.readFully(data);

			return data;
		}
	}

	private class IndexHelper {
		private static final int BYTE_PER_MESSAGE = 8;

		private static final int BYTE_PER_ENTRY = 8;

		private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

		private static final int ENTRY_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_ENTRY;

		private Header m_header = new Header();

		private FSDataInputStream m_indexSteam;

		public void close() {
		}

		public void init(FSDataInputStream indexStream) throws IOException {
			m_indexSteam = indexStream;
			int size = indexStream.available();
			int totalHeaders = (int) Math.ceil((size * 1.0 / (ENTRY_PER_SEGMENT * SEGMENT_SIZE)));

			if (totalHeaders == 0) {
				totalHeaders = 1;
			}

			for (int i = 0; i < totalHeaders; i++) {
				m_header.load(i);
			}
		}

		public boolean isOpen() {
			return m_indexSteam != null;
		}

		public long read(MessageId id) throws IOException {
			int index = id.getIndex();
			long position = m_header.getOffset(id.getIpAddressValue(), index);

			if (position > 0) {
				m_indexSteam.seek(position);

				long address = m_indexSteam.readLong();

				return address;
			}
			return -1;
		}

		private class Header {
			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			private Integer findSegment(int ip, int index) throws IOException {
				Map<Integer, Integer> map = m_table.get(ip);

				if (map != null) {
					return map.get(index);
				}
				return null;
			}

			public long getOffset(int ip, int seq) throws IOException {
				int segmentIndex = seq / MESSAGE_PER_SEGMENT;
				int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
				Integer segmentId = findSegment(ip, segmentIndex);

				if (segmentId != null) {
					long offset = segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

					return offset;
				} else {
					return -1;
				}
			}

			public void load(int headBlockIndex) throws IOException {
				Segment segment = new Segment(m_indexSteam, headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
				long magicCode = segment.readLong();

				if (magicCode != -1) {
					throw new IOException("Invalid index file: " + m_indexSteam);
				}

				m_nextSegment = 1 + ENTRY_PER_SEGMENT * headBlockIndex;

				int readerIndex = 1;

				while (readerIndex < ENTRY_PER_SEGMENT) {
					int ip = segment.readInt();
					int index = segment.readInt();

					readerIndex++;

					if (ip != 0) {
						Map<Integer, Integer> map = m_table.get(ip);

						if (map == null) {
							map = new HashMap<Integer, Integer>();
							m_table.put(ip, map);
						}

						Integer segmentNo = map.get(index);

						if (segmentNo == null) {
							segmentNo = m_nextSegment++;

							map.put(index, segmentNo);
						}
					} else {
						break;
					}
				}
			}
		}

		private class Segment {

			private long m_address;

			private ByteBuffer m_buf;

			private Segment(FSDataInputStream channel, long address) throws IOException {
				m_address = address;
				byte[] b = new byte[SEGMENT_SIZE];

				channel.readFully(b);
				m_buf = ByteBuffer.wrap(b);
			}

			public int readInt() throws IOException {
				return m_buf.getInt();
			}

			public long readLong() throws IOException {
				return m_buf.getLong();
			}

			@Override
			public String toString() {
				return String.format("%s[address=%s]", getClass().getSimpleName(), m_address);
			}
		}
	}

}
