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
package com.dianping.cat.consumer.dump;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.message.storage.MessageBlock;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.statistic.ServerStatisticManager;
import io.netty.buffer.ByteBuf;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LocalMessageBucketManager extends ContainerHolder
						implements MessageBucketManager, Initializable,	LogEnabled {

	public static final String ID = "local";

	protected Logger m_logger;

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private PathBuilder m_pathBuilder;

	private ConcurrentHashMap<String, LocalMessageBucket> m_buckets = new ConcurrentHashMap<String, LocalMessageBucket>();

	private File m_baseDir;

	private String m_localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	private long m_total;

	private int m_gzipThreads = 20;

	private int m_gzipMessageSize = 5000;

	private int m_messageBlockSize = 5000;

	private BlockingQueue<MessageBlock> m_messageBlocks = new LinkedBlockingQueue<MessageBlock>(m_messageBlockSize);

	private List<BlockingQueue<MessageItem>> m_messageQueues = new ArrayList<BlockingQueue<MessageItem>>();

	private BlockingQueue<MessageItem> m_last;

	@Override
	public void archive(long startTime) {
		String path = m_pathBuilder.getLogviewPath(new Date(startTime), "");
		List<String> keys = new ArrayList<String>();

		for (String key : m_buckets.keySet()) {
			if (key.startsWith(path)) {
				keys.add(key);
			}
		}
		for (String key : keys) {
			try {
				LocalMessageBucket bucket = m_buckets.get(key);
				MessageBlock block = bucket.flushBlock();

				if (block != null) {
					m_messageBlocks.put(block);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<String> findCloseBuckets() {
		final Set<String> paths = new HashSet<String>();

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (shouldUpload(path)) {
						int index = path.indexOf(".idx");

						if (index == -1) {
							paths.add(path);
						} else {
							paths.add(path.substring(0, index));
						}
					}
				}
				return Direction.DOWN;
			}
		});
		return new ArrayList<String>(paths);
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_configManager.isUseNewStorage()) {
			m_baseDir = new File(m_configManager.getHdfsLocalBaseDir(ServerConfigManager.DUMP_DIR));

			Threads.forGroup("cat").start(new BlockDumper(m_buckets, m_messageBlocks, m_serverStateManager, m_configManager));
			Threads.forGroup("cat").start(new CloseBucketChecker());

			if (m_configManager.isLocalMode()) {
				m_gzipThreads = 2;
			}

			for (int i = 0; i < m_gzipThreads; i++) {
				LinkedBlockingQueue<MessageItem> messageQueue = new LinkedBlockingQueue<MessageItem>(m_gzipMessageSize);

				m_messageQueues.add(messageQueue);
				Threads.forGroup("cat").start(new MessageGzip(messageQueue, i));
			}
			m_last = m_messageQueues.get(m_gzipThreads - 1);
		}
	}

	@Override
	public MessageTree loadMessage(String messageId) {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("BucketService", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);

		try {
			MessageId id = MessageId.parse(messageId);
			final String path = m_pathBuilder.getLogviewPath(new Date(id.getTimestamp()), "");
			final File dir = new File(m_baseDir, path);
			final String key = id.getDomain() + '-' + id.getIpAddress();
			final List<String> paths = new ArrayList<String>();

			Scanners.forDir().scan(dir, new FileMatcher() {
				@Override
				public Direction matches(File base, String name) {
					if (name.contains(key) && !name.endsWith(".idx")) {
						paths.add(path + name);
					}
					return Direction.NEXT;
				}
			});

			for (String dataFile : paths) {
				LocalMessageBucket bucket = m_buckets.get(dataFile);

				if (bucket != null) {
					MessageBlock block = bucket.flushBlock();

					if (block != null) {
						boolean first = m_messageBlocks.offer(block);

						LockSupport.parkNanos(200 * 1000 * 1000L); // wait 200 ms

						if (!first) {
							boolean retry = m_messageBlocks.offer(block);

							if (!retry) {
								Cat.logError(new RuntimeException("error flush block when read logview"));
							} else {
								LockSupport.parkNanos(200 * 1000 * 1000L); // wait 200 ms
							}
						}
					}
					MessageTree tree = bucket.findById(messageId);

					if (tree != null && tree.getMessageId().equals(messageId)) {
						t.addData("path", dataFile);
						return tree;
					}
				} else {
					File file = new File(m_baseDir, dataFile);

					if (file.exists()) {
						try {
							bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
							bucket.setBaseDir(m_baseDir);
							bucket.initialize(dataFile);

							MessageTree tree = bucket.findById(messageId);

							if (tree != null && tree.getMessageId().equals(messageId)) {
								t.addData("path", dataFile);
								return tree;
							}
						} catch (Exception e) {
							Cat.logError(e);
						} finally {
							bucket.close();
							release(bucket);
						}
					}
				}
			}
			return null;
		} catch (Throwable e) {
			t.setStatus(e);
			cat.logError(e);
		} finally {
			t.complete();
		}
		return null;
	}

	private void logStorageState(final MessageTree tree) {
		String domain = tree.getDomain();
		int size = tree.getBuffer().readableBytes();

		m_serverStateManager.addMessageSize(domain, size);
		if ((++m_total) % CatConstants.SUCCESS_COUNT == 0) {
			m_serverStateManager.addMessageDump(CatConstants.SUCCESS_COUNT);
		}
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	public void setLocalIp(String localIp) {
		m_localIp = localIp;
	}

	private boolean shouldUpload(String path) {
		long current = System.currentTimeMillis();
		long currentHour = current - current % TimeHelper.ONE_HOUR;
		long lastHour = currentHour - TimeHelper.ONE_HOUR;
		long nextHour = currentHour + TimeHelper.ONE_HOUR;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/HH");
		String currentHourStr = sdf.format(new Date(currentHour));
		String lastHourStr = sdf.format(new Date(lastHour));
		String nextHourStr = sdf.format(new Date(nextHour));

		int indexOf = path.indexOf(currentHourStr);
		int indexOfLast = path.indexOf(lastHourStr);
		int indexOfNext = path.indexOf(nextHourStr);

		if (indexOf > -1 || indexOfLast > -1 || indexOfNext > -1) {
			return false;
		}
		return true;
	}

	@Override
	public void storeMessage(final MessageTree tree, final MessageId id) {
		boolean errorFlag = true;
		int hash = Math.abs((id.getDomain() + '-' + id.getIpAddress()).hashCode());
		int index = (int) (hash % m_gzipThreads);
		MessageItem item = new MessageItem(tree, id);
		BlockingQueue<MessageItem> queue = m_messageQueues.get(index % (m_gzipThreads - 1));
		boolean result = queue.offer(item);

		if (result) {
			errorFlag = false;
		} else {
			if (m_last.offer(item)) {
				errorFlag = false;
			}
		}

		if (errorFlag) {
			m_serverStateManager.addMessageDumpLoss(1);
		}
		logStorageState(tree);
	}

	public class CloseBucketChecker implements Task {

		private void closeBuckets(final List<String> paths) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

			for (String path : paths) {
				LocalMessageBucket bucket = m_buckets.remove(path);

				if (bucket != null) {
					try {
						bucket.close();
						Cat.logEvent("CloseBucket", ip);
					} catch (Exception e) {
						Cat.logError(e);
					} finally {
						m_buckets.remove(path);
						release(bucket);
					}
				}
			}
		}

		@Override
		public String getName() {
			return "LocalMessageBucketManager-CloseBucketChecker";
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(TimeHelper.ONE_MINUTE);

					try {
						List<String> paths = findCloseBuckets();

						closeBuckets(paths);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
		}
	}

	public class MessageGzip implements Task {

		public BlockingQueue<MessageItem> m_messageQueue;

		private int m_index;

		private int m_count = -1;

		public MessageGzip(BlockingQueue<MessageItem> messageQueue, int index) {
			m_messageQueue = messageQueue;
			m_index = index;
		}

		@Override
		public String getName() {
			return "Message-Gzip-" + m_index;
		}

		private void gzipMessage(MessageItem item) {
			try {
				MessageId id = item.getMessageId();
				String name = id.getDomain() + '-' + id.getIpAddress() + '-' + m_localIp;
				String path = m_pathBuilder.getLogviewPath(new Date(id.getTimestamp()), name);
				LocalMessageBucket bucket = m_buckets.get(path);

				if (bucket == null) {
					synchronized (m_buckets) {
						bucket = m_buckets.get(path);
						if (bucket == null) {
							bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
							bucket.setBaseDir(m_baseDir);
							bucket.initialize(path);

							m_buckets.put(path, bucket);
						}
					}
				}

				DefaultMessageTree tree = (DefaultMessageTree) item.getTree();
				ByteBuf buf = tree.getBuffer();
				MessageBlock bolck = bucket.storeMessage(buf, id);

				if (bolck != null) {
					if (!m_messageBlocks.offer(bolck)) {
						m_serverStateManager.addBlockLoss(1);
						Cat.logEvent("DumpError", tree.getDomain());
					}
				}
			} catch (Throwable e) {
				Cat.logError(e);
			}
		}

		private void gzipMessageWithMonitor(MessageItem item) {
			Transaction t = Cat.newTransaction("Gzip", "Thread-" + m_index);
			t.setStatus(Transaction.SUCCESS);

			gzipMessage(item);
			t.complete();
		}

		@Override
		public void run() {
			try {
				while (true) {
					MessageItem item = m_messageQueue.poll(5, TimeUnit.MILLISECONDS);

					if (item != null) {
						m_count++;
						if (m_count % (10000) == 0) {
							gzipMessageWithMonitor(item);
						} else {
							gzipMessage(item);
						}
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
