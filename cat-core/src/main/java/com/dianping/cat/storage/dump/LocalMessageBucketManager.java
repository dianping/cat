package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.unidal.helper.Files;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.status.ServerStateManager;

public class LocalMessageBucketManager extends ContainerHolder implements MessageBucketManager, Initializable,
      LogEnabled {
	public static final String ID = "local";

	private static final long ONE_HOUR = 60 * 60 * 1000L;

	private File m_baseDir;

	private Map<String, LocalMessageBucket> m_buckets = new HashMap<String, LocalMessageBucket>();

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private ServerStateManager m_serverStateManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private String m_localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	private int m_error;

	private int m_total;

	private long m_totalSize = 0;

	private long m_lastTotalSize = 0;

	private Logger m_logger;

	private int m_gzipThreads = 3;

	private BlockingQueue<MessageBlock> m_messageBlocks = new LinkedBlockingQueue<MessageBlock>(10000);

	private Map<Integer, LinkedBlockingQueue<MessageItem>> m_messageQueues = new HashMap<Integer, LinkedBlockingQueue<MessageItem>>();

	private int[] m_processMessages = new int[m_gzipThreads];

	public void archive(long startTime) {
		String path = m_pathBuilder.getPath(new Date(startTime), "");
		List<String> keys = new ArrayList<String>();

		synchronized (m_buckets) {
			for (String key : m_buckets.keySet()) {
				if (key.startsWith(path)) {
					keys.add(key);
				}
			}

			Transaction t = Cat.newTransaction("System", "Dump");
			t.setStatus(Message.SUCCESS);

			for (String key : keys) {
				LocalMessageBucket bucket = m_buckets.remove(key);

				try {
					bucket.close();
				} catch (IOException e) {
					// ignore
				}

				try {
					bucket.archive();
				} catch (Exception e) {
					Cat.getProducer().logError(e);
				}
			}

			t.complete();
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (m_buckets) {
			for (LocalMessageBucket bucket : m_buckets.values()) {
				bucket.close();
			}

			m_buckets.clear();
		}
	}

	void closeIdleBuckets() throws IOException {
		long now = System.currentTimeMillis();
		long hour = 3600 * 1000L;
		List<String> closedKeys = new ArrayList<String>();

		synchronized (m_buckets) {
			for (Map.Entry<String, LocalMessageBucket> e : m_buckets.entrySet()) {
				LocalMessageBucket bucket = e.getValue();

				if (now - bucket.getLastAccessTime() >= hour) {
					bucket.close();
					closedKeys.add(e.getKey());
				}
			}

			for (String key : closedKeys) {
				m_buckets.remove(key);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_baseDir == null) {
			m_baseDir = new File(m_configManager.getHdfsLocalBaseDir("dump"));
		}

		Threads.forGroup("Cat").start(new BlockDumper());
		Threads.forGroup("Cat").start(new IdleChecker());
		Threads.forGroup("Cat").start(new OldMessageMover());

		for (int i = 0; i < m_gzipThreads; i++) {
			Threads.forGroup("Cat").start(new MessageGzip(i));
		}
	}

	@Override
	public MessageTree loadMessage(String messageId) throws IOException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("BucketService", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);

		try {
			MessageId id = MessageId.parse(messageId);
			final String path = m_pathBuilder.getPath(new Date(id.getTimestamp()), "");
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

				if (bucket == null) {
					File file = new File(m_baseDir, dataFile);

					if (file.exists()) {
						bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
						bucket.setBaseDir(m_baseDir);
						bucket.initialize(dataFile);
						m_buckets.put(dataFile, bucket);
					}
				}

				if (bucket != null) {
					// flush the buffer if have data
					MessageBlock block = bucket.flushBlock();

					if (block != null) {
						m_messageBlocks.offer(block);

						LockSupport.parkNanos(50 * 1000 * 1000L); // wait 50 ms
					}

					MessageTree tree = bucket.findByIndex(id.getIndex());

					if (tree != null && tree.getMessageId().equals(messageId)) {
						t.addData("path", dataFile);
						return tree;
					}
				}
			}

			return null;
		} catch (IOException e) {
			t.setStatus(e);
			cat.logError(e);
			throw e;
		} catch (RuntimeException e) {
			t.setStatus(e);
			cat.logError(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			cat.logError(e);
			throw e;
		} finally {
			t.complete();
		}
	}

	private void moveOldMessages() {
		final List<String> paths = new ArrayList<String>();

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (shouldMove(path)) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});
		if (paths.size() > 0) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			Transaction t = Cat.newTransaction("System", "Dump" + "-" + ip);
			t.setStatus(Message.SUCCESS);

			for (String path : paths) {
				try {
					Cat.getProducer().logEvent("Dump", "Outbox.Abnormal", Message.SUCCESS, path);

					File outbox = new File(m_baseDir, "outbox");
					File from = new File(m_baseDir, path);
					File to = new File(outbox, path);

					to.getParentFile().mkdirs();
					Files.forDir().copyFile(from, to);
					Files.forDir().delete(from);

					File parentFile = from.getParentFile();

					parentFile.delete(); // delete it if empty
					parentFile.getParentFile().delete(); // delete it if empty
				} catch (Exception e) {
					t.setStatus(Message.SUCCESS);
					Cat.logError(e);
				}
			}

			t.complete();
		}
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	private boolean shouldMove(String path) {
		if (path.indexOf("draft") > -1 || path.indexOf("outbox") > -1) {
			return false;
		}

		long current = System.currentTimeMillis();
		long currentHour = current - current % ONE_HOUR;
		long lastHour = currentHour - ONE_HOUR;
		long nextHour = currentHour + ONE_HOUR;

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
	public void storeMessage(final MessageTree tree, final MessageId id) throws IOException {
		String domain = id.getDomain() + id.getIpAddress();
		int bucketIndex = Math.abs(domain.hashCode()) % m_gzipThreads;

		if (bucketIndex > m_gzipThreads || bucketIndex < 0) {
			m_logger.error("Error where compute the message bucket index!" + bucketIndex);
		} else {
			m_processMessages[bucketIndex]++;
		}

		LinkedBlockingQueue<MessageItem> items = m_messageQueues.get(bucketIndex);

		if (items == null) {
			items = new LinkedBlockingQueue<LocalMessageBucketManager.MessageItem>(10000);
			m_messageQueues.put(bucketIndex, items);
		}

		boolean result = items.offer(new MessageItem(tree, id));

		if (result == false) {
			m_error++;
			if (m_error % CatConstants.ERROR_COUNT == 0) {
				m_serverStateManager.addMessageDumpLoss(CatConstants.ERROR_COUNT);
				m_logger.error("Error when offer message tree to gzip queue! overflow :" + m_error);
			}
		}

		m_total++;
		if (m_total % (CatConstants.SUCCESS_COUNT) == 0) {
			logState(tree);
		}
	}

	private void logState(final MessageTree tree) {
		double amount = m_totalSize - m_lastTotalSize;
		m_lastTotalSize = m_totalSize;

		m_serverStateManager.addMessageDump(CatConstants.SUCCESS_COUNT);
		m_serverStateManager.addMessageSize(amount);

		Message message = tree.getMessage();
		if (message instanceof Transaction) {
			long delay = System.currentTimeMillis() - tree.getMessage().getTimestamp()
			      - ((Transaction) message).getDurationInMillis();
			m_serverStateManager.addProcessDelay(delay);
		}
		if (m_total % (CatConstants.SUCCESS_COUNT * 1000) == 0) {
			m_logger.info("Dump message number: " + m_total + " Size:" + m_totalSize * 1.0 / 1024 / 1024 / 1024 + "GB");

			StringBuilder sb = new StringBuilder("GzipThread Process Message Number :");
			for (int i = 0; i < m_gzipThreads; i++) {
				sb.append(m_processMessages[i] + " \t");
			}
			m_logger.info(sb.toString());
		}
	}

	class MessageGzip implements Task {

		public int m_index;

		public MessageGzip(int index) {
			m_index = index;
		}

		@Override
		public void run() {
			try {
				while (true) {
					BlockingQueue<MessageItem> items = m_messageQueues.get(m_index);
					if (items != null) {
						MessageItem item = items.poll(5, TimeUnit.MILLISECONDS);

						if (item != null) {
							try {
								MessageTree tree = item.getTree();
								MessageId id = item.getMessageId();

								String name = id.getDomain() + '-' + id.getIpAddress() + '-' + m_localIp;
								String dataFile = m_pathBuilder.getPath(new Date(id.getTimestamp()), name);
								LocalMessageBucket bucket = m_buckets.get(dataFile);

								if (bucket == null) {
									bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
									bucket.setBaseDir(m_baseDir);
									bucket.initialize(dataFile);
									m_buckets.put(dataFile, bucket);
								}

								DefaultMessageTree defaultTree = (DefaultMessageTree) tree;
								ChannelBuffer buf = defaultTree.getBuf();

								int size = buf.readableBytes();
								m_totalSize += size;

								MessageBlock bolck = bucket.storeMessage(buf, id);

								if (bolck != null) {
									if (!m_messageBlocks.offer(bolck)) {
										m_logger.error("Error when offer the block to the dump!");
									}
								}
							} catch (Exception e) {
								Cat.logError(e);
							}
						}
					}

				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public String getName() {
			return "Message Gizp " + m_index;
		}

		@Override
		public void shutdown() {

		}
	}

	class MessageItem {
		private MessageTree m_tree;

		private MessageId m_messageId;

		public MessageItem(MessageTree tree, MessageId messageId) {
			m_tree = tree;
			m_messageId = messageId;
		}

		public MessageTree getTree() {
			return m_tree;
		}

		public void setTree(MessageTree tree) {
			m_tree = tree;
		}

		public MessageId getMessageId() {
			return m_messageId;
		}

		public void setMessageId(MessageId messageId) {
			m_messageId = messageId;
		}

	}

	class BlockDumper implements Task {
		private int m_errors;

		@Override
		public String getName() {
			return "LocalMessageBucketManager-BlockDumper";
		}

		@Override
		public void run() {
			try {
				while (true) {
					MessageBlock block = m_messageBlocks.poll(5, TimeUnit.MILLISECONDS);

					if (block != null) {
						String dataFile = block.getDataFile();
						LocalMessageBucket bucket = m_buckets.get(dataFile);

						try {
							bucket.getWriter().writeBlock(block);
						} catch (Throwable e) {
							m_errors++;

							if (m_errors == 1 || m_errors % 100 == 0) {
								Cat.getProducer().logError(
								      new RuntimeException("Error when dumping for bucket: " + dataFile + ".", e));
							}
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

	class IdleChecker implements Task {
		@Override
		public String getName() {
			return "LocalMessageBucketManager-IdleChecker";
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(60 * 1000L); // 1 minute

					try {
						closeIdleBuckets();
					} catch (Throwable e) {
						Cat.getProducer().logError(e);
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

	class OldMessageMover implements Task {
		@Override
		public String getName() {
			return "LocalMessageBucketManager-OldMessageMover";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					moveOldMessages();
				} catch (Throwable e) {
					m_logger.error(e.getMessage(), e);
				}
				try {
					Thread.sleep(2 * 60 * 1000L);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
