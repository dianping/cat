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
package com.dianping.cat.message.context;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.util.Files;
import com.dianping.cat.util.Splitters;

// Component
public class MessageIdFactory implements Initializable {
	public static final long HOUR = 3600 * 1000L;

	private File m_baseDir;

	private String m_ipAddress;

	private AtomicBoolean m_initialized = new AtomicBoolean();

	// builder for current domain
	private Builder m_builder;

	private Map<String, Builder> m_builders = new HashMap<String, Builder>();

	// for test
	void clear() {
		Files.forDir().delete(m_baseDir, true);
	}

	public void close() {
		if (m_initialized.get()) {
			for (Builder builder : m_builders.values()) {
				builder.close();
			}
		}
	}

	private Builder findOrCreateBuilder(String domain) {
		if (domain == null) {
			return m_builder;
		}

		Builder builder = m_builders.get(domain);

		if (builder == null) {
			synchronized (m_builders) {
				builder = m_builders.get(domain);

				if (builder == null) {
					builder = new Builder(domain);
					m_builders.put(domain, builder);
				}
			}
		}

		return builder;
	}

	protected int getBatchSize() {
		return 100;
	}

	protected long getHour() {
		long timestamp = System.currentTimeMillis();

		return timestamp / HOUR;
	}

	// for test
	int getIndex() {
		if (m_initialized.get()) {
			Builder builder = findOrCreateBuilder(null);

			if (builder != null) {
				return builder.getIndex(getHour());
			} else {
				return 0;
			}
		} else {
			throw new IllegalStateException("Please call MessageIdFactory.initialize(String) first!");
		}
	}

	protected String getIpAddress() {
		if (m_ipAddress == null) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			List<String> items = Splitters.by('.').noEmptyItem().split(ip);

			if (items.size() == 4) {
				byte[] bytes = new byte[4];

				for (int i = 0; i < 4; i++) {
					bytes[i] = (byte) Integer.parseInt(items.get(i));
				}

				StringBuilder sb = new StringBuilder(bytes.length / 2);

				for (byte b : bytes) {
					sb.append(Integer.toHexString((b >> 4) & 0x0F));
					sb.append(Integer.toHexString(b & 0x0F));
				}

				m_ipAddress = sb.toString();
			} else {
				System.out.println("[ERROR] Unrecognized IP: " + ip + "!");

				m_ipAddress = "7f000001";
			}
		}

		return m_ipAddress;
	}

	public String getNextId() {
		return getNextId(null);
	}

	public String getNextId(String domain) {
		if (m_initialized.get()) {
			Builder builder = findOrCreateBuilder(domain);

			if (builder != null) {
				return builder.buildNextId();
			} else {
				return "";
			}
		} else {
			throw new IllegalStateException("Please call MessageIdFactory.initialize(String) first!");
		}
	}

	void initialize(File baseDir, String domain) {
		m_baseDir = baseDir;
		m_baseDir.mkdirs();
		m_builder = findOrCreateBuilder(domain);
		m_initialized.set(true);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	}

	public void initialize(String domain) {
		initialize(new File(Cat.getCatHome(), "mark"), domain);
	}

	public void saveMark() {

	}

	public void setDomain(String domain) {
		if (m_initialized.get()) {
			m_builder = findOrCreateBuilder(domain);
		} else {
			throw new IllegalStateException("Please call MessageIdFactory.initialize(String) first!");
		}
	}

	class Builder {
		private String m_domain;

		private AtomicLong m_lastHour = new AtomicLong();

		private AtomicInteger m_batchStart;

		private AtomicInteger m_batchOffset;

		private RandomAccessFile m_markFile;

		private MappedByteBuffer m_byteBuffer;

		public Builder(String domain) {
			File file = new File(m_baseDir, domain + ".mark");

			m_domain = domain;
			m_batchStart = new AtomicInteger();
			m_batchOffset = new AtomicInteger();

			try {
				m_markFile = new RandomAccessFile(file, "rw");
				m_byteBuffer = m_markFile.getChannel().map(MapMode.READ_WRITE, 0, 20);
			} catch (Throwable e) {
				throw new IllegalStateException(String.format("Unable to access mark file(%s)!", file), e);
			}
		}

		public String buildNextId() {
			StringBuilder sb = new StringBuilder(m_domain.length() + 32);
			long hour = getHour();

			sb.append(m_domain);
			sb.append('-');
			sb.append(getIpAddress());
			sb.append('-');
			sb.append(hour);
			sb.append('-');
			sb.append(getIndex(hour));

			return sb.toString();
		}

		public void close() {
			try {
				m_markFile.close();
			} catch (Exception e) {
				// ignore it
			}
		}

		private synchronized int getIndex(long hour) {
			int offset = m_batchOffset.incrementAndGet();

			if (m_lastHour.get() != hour || offset >= getBatchSize()) {
				FileLock lock = null;

				try {
					// lock could be null in case of CAT is stopping in progress
					lock = lock();

					int limit = m_byteBuffer.limit();
					long lastHour = limit >= 12 ? m_byteBuffer.getLong(4) : 0;

					if (lastHour == hour) { // same hour
						int start = limit >= 4 ? m_byteBuffer.getInt(0) : 0;

						m_batchStart.set(start);
					} else {
						m_batchStart.set(0);
					}

					offset = 0;
					m_lastHour.set(hour);
					m_batchOffset.set(0);
					m_byteBuffer.putInt(0, m_batchStart.get() + getBatchSize());
					m_byteBuffer.putLong(4, hour);

					if (lock != null) {
						m_markFile.getChannel().force(false);
					}
				} catch (InterruptedException e) {
					// ignore it
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					if (lock != null) {
						try {
							lock.release();
						} catch (Exception e) {
							// ignore it
						}
					}
				}
			}

			return m_batchStart.get() + offset;
		}

		private FileLock lock() throws InterruptedException {
			FileLock lock = null;

			while (lock == null) {
				try {
					lock = m_markFile.getChannel().tryLock();
				} catch (ClosedChannelException e) {
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				if (lock == null) {
					TimeUnit.MILLISECONDS.sleep(1);
				}
			}

			return lock;
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		ConfigureManager configureManager = ctx.lookup(ConfigureManager.class);

		initialize(configureManager.getDomain());
	}
}
