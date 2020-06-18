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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.util.CleanupHelper;

@Named
public class MessageIdFactory {
	public static final long HOUR = 3600 * 1000L;

	private volatile long m_timestamp = getTimestamp();

	private volatile AtomicInteger m_index = new AtomicInteger(0);

	private String m_domain = "UNKNOWN";

	private String m_ipAddress;
	
	private int m_processID=0;

	private MappedByteBuffer m_byteBuffer;

	private RandomAccessFile m_markFile;

	private Map<String, AtomicInteger> m_map = new ConcurrentHashMap<String, AtomicInteger>(100);

	private int m_retry;

	private String m_idPrefix;

	private String m_idPrefixOfMultiMode;
	
	public void close() {
		try {
			saveMark();
			if( m_byteBuffer != null ) {
				synchronized (m_byteBuffer) {
					CleanupHelper.cleanup(m_byteBuffer);
					m_byteBuffer = null;
				}
			}
			if( m_markChannel != null ) {
				m_markChannel.close();
				m_markChannel = null;
			}
			if( m_markFile != null ) {
				m_markFile.close();
				m_markFile = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ignore it
		}
	}
	

	private File createMarkFile(String domain) {
		File mark = new File(Cat.getCatHome(), "cat-" + domain + ".mark");

		if (!mark.exists()) {
			boolean success = true;
			try {
				success = mark.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
			}
			if (!success) {
				mark = createTempFile(domain);
			}
		} else if (!mark.canWrite()) {
			mark = createTempFile(domain);
		}
		return mark;
	}

	private File createTempFile(String domain) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File mark = new File(tmpDir, "cat-" + domain + ".mark");

		return mark;
	}

	public String getNextId() {
		long timestamp = getTimestamp();

		if (timestamp != m_timestamp) {
			synchronized (this) {
				if (timestamp != m_timestamp) {
					resetCounter(timestamp);
				}
			}
		}

		int index = m_index.getAndIncrement();
		StringBuilder sb = new StringBuilder(64);

		if (Cat.isMultiInstanceEnable()) {
			sb.append(m_idPrefixOfMultiMode).append(index);
		} else {
			sb.append(m_idPrefix).append(index);
		}

		return sb.toString();
	}

	public String getNextId(String domain) {
		if (domain.equals(m_domain)) {
			return getNextId();
		} else {
			long timestamp = getTimestamp();

			if (timestamp != m_timestamp) {
				synchronized (this) {
					if (timestamp != m_timestamp) {
						resetCounter(timestamp);
					}
				}
			}

			AtomicInteger value = m_map.get(domain);

			if (value == null) {
				synchronized (m_map) {
					value = m_map.get(domain);

					if (value == null) {
						value = new AtomicInteger(0);
						m_map.put(domain, value);
					}
				}
			}
			int index = value.getAndIncrement();
			StringBuilder sb = new StringBuilder(m_domain.length() + 32);

			if (Cat.isMultiInstanceEnable()) {
				sb.append(domain).append('-').append(m_ipAddress).append(".").append(m_processID).append('-').append(timestamp).append('-').append(index);
			} else {
				sb.append(domain).append('-').append(m_ipAddress).append('-').append(timestamp).append('-').append(index);
			}

			return sb.toString();
		}
	}

	private int getProcessID() {
		int retInt = -1;
		try {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			retInt = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
		} catch (Exception e) {
			Cat.logError(e);
		}

		if (retInt <= 0) {
			Random rd = new Random();
			// 保证数字大于0
			retInt = rd.nextInt(2 ^ 16) + 1;
		}

		return retInt;
	}

	protected long getTimestamp() {
		long timestamp = System.currentTimeMillis();

		return timestamp / HOUR; // version 2
	}
	
	String genIpHex() {
		String ip =  NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		List<String> items = Splitters.by(".").noEmptyItem().split(ip);
		byte[] bytes = new byte[4];

		for (int i = 0; i < 4; i++) {
			bytes[i] = (byte) Integer.parseInt(items.get(i));
		}

		StringBuilder sb = new StringBuilder(bytes.length / 2);

		for (byte b : bytes) {
			sb.append(Integer.toHexString((b >> 4) & 0x0F));
			sb.append(Integer.toHexString(b & 0x0F));
		}
		return sb.toString();
	}
	
	private transient FileChannel m_markChannel;

	public void initialize(String domain) throws IOException {
		m_domain = domain;
		m_ipAddress = genIpHex();
		m_processID = getProcessID();
		if( m_markFile != null ) {
			synchronized (this) {
				close();
			}
		}
		File mark = createMarkFile(domain);
		m_markFile = new RandomAccessFile(mark, "rw");
		m_markChannel = m_markFile.getChannel();
		m_byteBuffer = m_markChannel.map(MapMode.READ_WRITE, 0, 1024 * 1024L);
		m_idPrefix = initIdPrefix(getTimestamp(), false);
		m_idPrefixOfMultiMode = initIdPrefix(getTimestamp(), true);

		if (m_byteBuffer.limit() > 0) {
			try {
				long lastTimestamp = m_byteBuffer.getLong();
				int index = m_byteBuffer.getInt();

				if (lastTimestamp == m_timestamp) { // for same hour
					m_index = new AtomicInteger(index + 1000);

					int mapLength = m_byteBuffer.getInt();

					for (int i = 0; i < mapLength; i++) {
						int domainLength = m_byteBuffer.getInt();
						byte[] domainArray = new byte[domainLength];

						m_byteBuffer.get(domainArray);
						int value = m_byteBuffer.getInt();

						m_map.put(new String(domainArray), new AtomicInteger(value + 1000));
					}
				} else {
					m_index = new AtomicInteger(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				m_retry++;

				if (m_retry == 1) {
					mark.delete();
					initialize(domain);
				}
			}
		}

		saveMark();
		if( !shutdownHookOn ) {
			synchronized (this) {
				if( !shutdownHookOn ) {
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							close();
						}
					});
				}
			}
			shutdownHookOn = true;
		}
	}
	private volatile boolean shutdownHookOn;

	private String initIdPrefix(long timestamp, boolean multiMode) {
		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		if (multiMode) {
			sb.append(m_domain).append('-').append(m_ipAddress).append(".").append(m_processID).append('-').append(timestamp)
									.append('-');
		} else {
			sb.append(m_domain).append('-').append(m_ipAddress).append('-').append(timestamp).append('-');
		}

		return sb.toString();
	}

	private void resetCounter(long timestamp) {
		m_index.set(0);

		for (Entry<String, AtomicInteger> entry : m_map.entrySet()) {
			entry.getValue().set(0);
		}

		m_idPrefix = initIdPrefix(timestamp, false);
		m_idPrefixOfMultiMode = initIdPrefix(timestamp, true);

		m_timestamp = timestamp;
	}
	public int getIndex() {
		return m_index.get();
	}

	public synchronized void saveMark() {
		if( m_byteBuffer == null ) {
			return;
		}
		try {
			m_byteBuffer.rewind();
			m_byteBuffer.putLong(m_timestamp);
			m_byteBuffer.putInt(m_index.get());
			m_byteBuffer.putInt(m_map.size());

			for (Entry<String, AtomicInteger> entry : m_map.entrySet()) {
				byte[] bytes = entry.getKey().toString().getBytes();

				m_byteBuffer.putInt(bytes.length);
				m_byteBuffer.put(bytes);
				m_byteBuffer.putInt(entry.getValue().get());
			}

			m_byteBuffer.force();
		} catch (Throwable e) {
			e.printStackTrace();
			// ignore it
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

}
