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

	private long m_error;

	private long m_total;

	private long m_totalSize;

	private long m_lastTotalSize;

	private Logger m_logger;

	private int m_gzipThreads = 10;

	private BlockingQueue<MessageBlock> m_messageBlocks = new LinkedBlockingQueue<MessageBlock>(10000);

	private Map<Integer, LinkedBlockingQueue<MessageItem>> m_messageQueues = new HashMap<Integer, LinkedBlockingQueue<MessageItem>>();

	private long[] m_processMessages;

	public void archive(long startTime) {
		String path = m_pathBuilder.getPath(new Date(startTime), "");
		List<String> keys = new ArrayList<String>();

		synchronized (m_buckets) {
			for (String key : m_buckets.keySet()) {
				if (key.startsWith(path)) {
					keys.add(key);
				}
			}

			try {
				for (String key : keys) {
					LocalMessageBucket bucket = m_buckets.get(key);

					try {
						MessageBlock block = bucket.flushBlock();

						if (block != null) {
							m_messageBlocks.add(block);
						}
					} catch (IOException e) {
						Cat.logError(e);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
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
		Threads.forGroup("Cat").start(new OldMessageMover());

		if (m_configManager.isLocalMode()) {
			m_gzipThreads = 1;
		}
		m_processMessages = new long[m_gzipThreads];

		for (int i = 0; i < m_gzipThreads; i++) {
			LinkedBlockingQueue<MessageItem> messageQueue = new LinkedBlockingQueue<MessageItem>(500000);

			m_messageQueues.put(i, messageQueue);
			Threads.forGroup("Cat").start(new MessageGzip(messageQueue, i));
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
						synchronized (m_buckets) {
							m_buckets.put(dataFile, bucket);
						}
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
					if (path.indexOf(".idx") == -1 && shouldMove(path)) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});

		if (paths.size() > 0) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			Transaction t = Cat.newTransaction("System", "Move" + "-" + ip);
			t.setStatus(Message.SUCCESS);

			for (String path : paths) {
				File file = new File(m_baseDir, path);
				String loginfo = "path:" + m_baseDir + "/" + path + ",file size: " + file.length();

				LocalMessageBucket bucket = m_buckets.get(path);
				if (bucket != null) {
					try {
						bucket.close();
						bucket.archive();

						Cat.getProducer().logEvent("Move", "Outbox.Normal", Message.SUCCESS, loginfo);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
						m_logger.error(e.getMessage(), e);
					}
					synchronized (m_buckets) {
						m_buckets.remove(path);
					}
				} else {
					try {
						moveFile(path);
						moveFile(path + ".idx");

						Cat.getProducer().logEvent("Move", "Outbox.Abnormal", Message.SUCCESS, loginfo);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
						m_logger.error(e.getMessage(), e);
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
			t.complete();
		}
	}

	private void moveFile(String path) throws IOException {
		File outbox = new File(m_baseDir, "outbox");
		File from = new File(m_baseDir, path);
		File to = new File(outbox, path);

		to.getParentFile().mkdirs();
		Files.forDir().copyFile(from, to);
		Files.forDir().delete(from);

		File parentFile = from.getParentFile();

		parentFile.delete(); // delete it if empty
		parentFile.getParentFile().delete(); // delete it if empty
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
		// the message tree of one ip in the same hour should be put in one gzip thread
		String key = id.getDomain() + id.getIpAddress() + id.getTimestamp();
		int abs = key.hashCode();

		if (abs < 0) {
			abs = -abs;
		}
		int bucketIndex = abs % m_gzipThreads;
		m_processMessages[bucketIndex]++;

		LinkedBlockingQueue<MessageItem> items = m_messageQueues.get(bucketIndex);
		boolean result = items.offer(new MessageItem(tree, id));

		if (!result) {
			m_error++;
			if (m_error % (CatConstants.ERROR_COUNT * 10) == 0) {
				m_logger.error("Error when offer message tree to gzip queue! overflow :" + m_error + ". Gzip thread :"
				      + bucketIndex);
			}
			m_serverStateManager.addMessageDumpLoss(1);
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
			int fiveMinute = 1000 * 60 * 5;

			if (delay < fiveMinute && delay > -fiveMinute) {
				m_serverStateManager.addProcessDelay(delay);
			} else {
				m_logger.error("Error when compute the delay duration, " + delay);
			}
		}
		if (m_total % (CatConstants.SUCCESS_COUNT * 1000) == 0) {
			m_logger.info("dump message number: " + m_total + " size:" + m_totalSize * 1.0 / 1024 / 1024 / 1024 + "GB");

			StringBuilder sb = new StringBuilder("gzip thread process message number :");
			for (int i = 0; i < m_gzipThreads; i++) {
				sb.append(m_processMessages[i] + "\t");
			}
			m_logger.info(sb.toString());
		}
	}

	class MessageGzip implements Task {

		private int m_index;

		private long m_count;

		public BlockingQueue<MessageItem> m_messageQueue;

		public MessageGzip(BlockingQueue<MessageItem> messageQueue, int index) {
			m_messageQueue = messageQueue;
			m_index = index;
		}

		@Override
		public void run() {
			try {
				while (true) { 
					MessageItem item = m_messageQueue.poll(5, TimeUnit.MILLISECONDS);

					if (item != null) {
						m_count++;
						if (m_count % (10 * CatConstants.SUCCESS_COUNT) == 0) {
							gzipMessage(item, true);
						} else {
							gzipMessage(item, false);
						}
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		private void gzipMessage(MessageItem item, boolean monitor) {
			Transaction t = null;

			if (monitor) {
				t = Cat.newTransaction("Gzip", "Thread-" + m_index);
			}
			try {
				MessageId id = item.getMessageId();
				String name = id.getDomain() + '-' + id.getIpAddress() + '-' + m_localIp;
				String dataFile = m_pathBuilder.getPath(new Date(id.getTimestamp()), name);
				LocalMessageBucket bucket = m_buckets.get(dataFile);

				if (bucket == null) {
					bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
					bucket.setBaseDir(m_baseDir);
					bucket.initialize(dataFile);
					synchronized (m_buckets) {
						m_buckets.put(dataFile, bucket);
               }
				}

				DefaultMessageTree tree = (DefaultMessageTree) item.getTree();
				ChannelBuffer buf = tree.getBuffer();
				MessageBlock bolck = bucket.storeMessage(buf, id);

				if (bolck != null) {
					if (!m_messageBlocks.offer(bolck)) {
						m_serverStateManager.addBlockLoss(1);
						m_logger.error("Error when offer the block to the dump!");
					}
				}
				m_totalSize += buf.readableBytes();

				if (t != null) {
					t.setStatus(Message.SUCCESS);
				}
			} catch (Throwable e) {
				Cat.logError(e);
				if (t != null) {
					t.setStatus(e);
				}
			} finally {
				if (t != null) {
					t.complete();
				}
			}
		}

		public int getIndex() {
			return m_index;
		}

		@Override
		public String getName() {
			return "Message-Gzip-" + m_index;
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

		private int m_success;

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
						long time = System.currentTimeMillis();
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
						m_serverStateManager.addBlockTotal(1);
						if ((++m_success) % 10000 == 0) {
							int size = m_messageBlocks.size();

							if (size > 0) {
								m_logger.info("block queue size " + size);
							}
						}
						long duration = System.currentTimeMillis() - time;
						m_serverStateManager.addBlockTime(duration);
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
