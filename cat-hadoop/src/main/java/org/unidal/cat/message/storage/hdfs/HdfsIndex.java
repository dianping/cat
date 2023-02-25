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

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Index.class, value = HdfsBucket.ID, instantiationStrategy = Named.PER_LOOKUP)
public class HdfsIndex implements Index {
	public static final String ID = "hdfs";

	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject
	protected HdfsSystemManager m_manager;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject("hdfs")
	private PathBuilder m_bulider;

	@Inject("hdfs")
	private TokenMappingManager m_hdfsTokenManager;

	private TokenMapping m_mapping;

	private MessageIdCodec m_codec = new MessageIdCodec();

	private IndexHelper m_index = new IndexHelper();

	private long m_lastAccessTime;

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_index.close();
		}
	}

	@Override
	public MessageId find(MessageId id) throws IOException {
		long value = m_index.read(id);

		if (value != 0) {
			byte[] data = getBytes(value);

			return m_codec.decode(data, id.getHour());
		} else {
			return null;
		}
	}

	public void flush() {
		throw new RuntimeException("unsupport operation");
	}

	private byte[] getBytes(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		return bytes;
	}

	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	@Override
	public void initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		FileSystem fs = m_manager.getFileSystem();
		String dataPath = m_bulider.getPath(domain, startTime, ip, FileType.MAPPING);
		FSDataInputStream indexStream = fs.open(new Path(dataPath));

		m_index.init(indexStream);
		m_mapping = m_hdfsTokenManager.getTokenMapping(hour, ip);
	}

	@Override
	public void map(MessageId from, MessageId to) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void maps(Map<MessageId, MessageId> maps) throws IOException {
		throw new RuntimeException("unsupport operation");
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

	protected class MessageIdCodec {

		private int bytesToInt(byte[] src, int offset) {
			int value = (int) (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16)	| ((src[offset + 2] & 0xFF) << 8)
									| (src[offset + 3] & 0xFF));
			return value;
		}

		private MessageId decode(byte[] data, int currentHour) throws IOException {
			int value = bytesToInt(data, 0);
			int index = bytesToInt(data, 4);

			int s1 = (value >> 17) & 0x00007FFF;
			int s2 = (value >> 2) & 0x00007FFF;
			int s3 = value & 0x0003;
			String domain = m_mapping.find(s1);
			String ipAddressInHex = m_mapping.find(s2);
			int flag = (s3 >> 14) & 0x03;
			int hour = currentHour + (flag == 3 ? -1 : flag);

			if (domain != null && ipAddressInHex != null) {
				return new MessageId(domain, ipAddressInHex, hour, index);
			} else {
				return null;
			}
		}
	}

}
