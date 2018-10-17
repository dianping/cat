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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.cat.message.storage.internals.ByteBufCache;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Index.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalIndex implements Index {
	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject("local")
	private PathBuilder m_bulider;

	@Inject("local")
	private TokenMappingManager m_manager;

	@Inject
	private ByteBufCache m_bufCache;

	private TokenMapping m_mapping;

	private MessageIdCodec m_codec = new MessageIdCodec();

	private IndexHelper m_index = new IndexHelper();

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

	private long getLong(byte[] bytes) {
		return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16))	| (
								0xff000000L & ((long) bytes[3] << 24)) | (0xff00000000L & ((long) bytes[4] << 32))	| (0xff0000000000L & (
								(long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48))	| (0xff00000000000000L & (
								(long) bytes[7] << 56));
	}

	@Override
	public void initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		File indexPath = new File(m_bulider.getPath(domain, startTime, ip, FileType.MAPPING));

		m_index.init(indexPath);
		m_mapping = m_manager.getTokenMapping(hour, ip);
	}

	@Override
	public void map(MessageId from, MessageId to) throws IOException {
		byte[] data = m_codec.encode(to, from.getHour());

		m_index.write(from, getLong(data));
	}

	@Override
	public void maps(Map<MessageId, MessageId> maps) throws IOException {
		for (Entry<MessageId, MessageId> entry : maps.entrySet()) {
			map(entry.getKey(), entry.getValue());
		}
	}

	private class IndexHelper {
		private static final int BYTE_PER_MESSAGE = 8;

		private static final int BYTE_PER_ENTRY = 8;

		private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

		private static final int ENTRY_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_ENTRY;

		private RandomAccessFile m_file;

		private File m_path;

		private FileChannel m_indexChannel;

		private Header m_header = new Header();

		private Map<String, SegmentCache> m_caches = new LinkedHashMap<String, SegmentCache>();

		private void close() {
			try {
				m_header.m_segment.close();

				for (SegmentCache cache : m_caches.values()) {
					cache.close();
				}
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_indexChannel.force(false);
				m_indexChannel.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_file = null;
			m_caches.clear();
		}

		private Segment getSegment(String ip, long id) throws IOException {
			SegmentCache cache = m_caches.get(ip);

			if (cache == null) {
				cache = new SegmentCache();
				m_caches.put(ip, cache);
			}

			return cache.findOrCreateNextSegment(id);
		}

		private void init(File indexPath) throws IOException {
			m_path = indexPath;
			m_path.getParentFile().mkdirs();

			// read-write without meta sync
			m_file = new RandomAccessFile(m_path, "rwd");
			m_indexChannel = m_file.getChannel();

			long size = m_file.length();
			int totalHeaders = (int) Math.ceil((size * 1.0 / (ENTRY_PER_SEGMENT * SEGMENT_SIZE)));

			if (totalHeaders == 0) {
				totalHeaders = 1;
			}

			for (int i = 0; i < totalHeaders; i++) {
				m_header.load(i);
			}
		}

		private boolean isOpen() {
			return m_file != null;
		}

		private long read(MessageId id) throws IOException {
			int index = id.getIndex();
			long position = m_header.getOffset(id.getIpAddressValue(), index, false);

			int segmentId = (int) (position / SEGMENT_SIZE);
			int offset = (int) (position % SEGMENT_SIZE);
			Segment segment = getSegment(id.getIpAddressInHex(), segmentId);

			if (segment != null) {
				try {
					long blockAddress = segment.readLong(offset);

					return blockAddress;
				} catch (EOFException e) {
					// ignore it
				}
			} else if (position > 0) {
				m_file.seek(position);

				long address = m_file.readLong();

				return address;
			}

			throw new RuntimeException("error when find message id:" + id.toString());
		}

		private void write(MessageId id, long value) throws IOException {
			long position = m_header.getOffset(id.getIpAddressValue(), id.getIndex(), true);
			long address = position / SEGMENT_SIZE;
			int offset = (int) (position % SEGMENT_SIZE);
			Segment segment = getSegment(id.getIpAddressInHex(), address);

			if (segment != null) {
				segment.writeLong(offset, value);
			} else {
				Cat.logEvent("Block", "Abnormal:" + id.getDomain(), Event.SUCCESS, null);
				m_indexChannel.position(position);

				ByteBuffer buf = ByteBuffer.allocate(8);
				buf.putLong(value);
				buf.flip();
				m_indexChannel.write(buf);
			}
		}

		private class Header {
			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			private Segment m_segment;

			private int m_offset;

			private Integer findSegment(int ip, int index, boolean createIfNotExists) throws IOException {
				Map<Integer, Integer> map = m_table.get(ip);

				if (map == null && createIfNotExists) {
					map = new HashMap<Integer, Integer>();
					m_table.put(ip, map);
				}

				Integer segmentId = map == null ? null : map.get(index);

				if (segmentId == null && createIfNotExists) {
					long value = (((long) ip) << 32) + index;

					segmentId = m_nextSegment;
					map.put(index, segmentId);

					m_segment.writeLong(m_offset, value);
					m_offset += 8;

					m_nextSegment++;

					if (m_nextSegment % (ENTRY_PER_SEGMENT) == 0) {
						// last segment is full, create new one
						m_segment.close();
						m_segment = new Segment(m_indexChannel, m_nextSegment * SEGMENT_SIZE);

						m_nextSegment++; // skip self head data
						m_segment.writeLong(0, -1);
						m_offset = 8;
					}
				}

				return segmentId;
			}

			private long getOffset(int ip, int seq, boolean createIfNotExists) throws IOException {
				int segmentIndex = seq / MESSAGE_PER_SEGMENT;
				int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
				Integer segmentId = findSegment(ip, segmentIndex, createIfNotExists);

				if (segmentId != null) {
					long offset = segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

					return offset;
				} else {
					return -1;
				}
			}

			private void load(int headBlockIndex) throws IOException {
				Segment segment = new Segment(m_indexChannel, headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
				long magicCode = segment.readLong();

				if (magicCode == 0) {
					segment.writeLong(0, -1);
				} else if (magicCode != -1) {
					throw new IOException("Invalid index file: " + m_path);
				}

				m_segment = segment;
				m_nextSegment = 1 + ENTRY_PER_SEGMENT * headBlockIndex;
				m_offset = 8;

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

						m_offset += 8;
					} else {
						break;
					}
				}
			}
		}

		private class Segment {
			private FileChannel m_segmentChannel;

			private long m_address;

			private ByteBuffer m_buf;

			private Segment(FileChannel channel, long address) throws IOException {
				m_segmentChannel = channel;
				m_address = address;

				m_buf = m_bufCache.get();
				// m_buf = ByteBuffer.allocate(SEGMENT_SIZE);
				m_buf.mark();
				m_segmentChannel.read(m_buf, address);
				m_buf.reset();
			}

			private void close() throws IOException {
				int pos = m_buf.position();

				m_buf.position(0);
				m_segmentChannel.write(m_buf, m_address);
				m_buf.position(pos);
				m_bufCache.put(m_buf);
			}

			private int readInt() throws IOException {
				return m_buf.getInt();
			}

			private long readLong() throws IOException {
				return m_buf.getLong();
			}

			private long readLong(int offset) throws IOException {
				return m_buf.getLong(offset);
			}

			@Override
			public String toString() {
				return String.format("%s[address=%s]", getClass().getSimpleName(), m_address);
			}

			private void writeLong(int offset, long value) throws IOException {
				m_buf.putLong(offset, value);
			}
		}

		private class SegmentCache {
			private final static int CACHE_SIZE = 2;

			private long m_maxSegmentId;

			private Map<Long, Segment> m_latestSegments = new LinkedHashMap<Long, Segment>();

			public void close() throws IOException {
				for (Segment segment : m_latestSegments.values()) {
					segment.close();
				}
				m_latestSegments.clear();
			}

			public Segment findOrCreateNextSegment(long segmentId) throws IOException {
				Segment segment = m_latestSegments.get(segmentId);

				if (segment == null) {
					if (segmentId > m_maxSegmentId) {
						if (m_latestSegments.size() >= CACHE_SIZE) {
							removeOldSegment();
						}

						segment = new Segment(m_indexChannel, segmentId * SEGMENT_SIZE);

						m_latestSegments.put(segmentId, segment);
						m_maxSegmentId = segmentId;
					} else {
						int duration = (int) (m_maxSegmentId - segmentId);
						Cat.logEvent("OldSegment", String.valueOf(duration), Event.SUCCESS,
												String.valueOf(segmentId)	+ ",max:" + String.valueOf(m_maxSegmentId));
					}
				}

				return segment;
			}

			private void removeOldSegment() throws IOException {
				Entry<Long, Segment> first = m_latestSegments.entrySet().iterator().next();
				Segment segment = m_latestSegments.remove(first.getKey());

				segment.close();
			}
		}
	}

	/**
		* domain 15bits
		* ip     15bits
		* hour   2 bits
		* seq    32bits
		*/
	class MessageIdCodec {

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

			return new MessageId(domain, ipAddressInHex, hour, index);
		}

		private byte[] encode(MessageId id, int currentHour) throws IOException {
			int domainIndex = m_mapping.map(id.getDomain());
			int ipIndex = m_mapping.map(id.getIpAddressInHex());
			int hour = id.getHour() - currentHour;
			int seq = id.getIndex();
			ByteBuf buf = Unpooled.buffer(8);
			int value = (domainIndex << 17) + (ipIndex << 2) + (hour);

			buf.writeInt(value);
			buf.writeInt(seq);

			byte[] data = buf.array();

			return data;
		}
	}

}
