package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Scanners;
import com.site.helper.Scanners.FileMatcher;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class LocalMessageBucketManager extends ContainerHolder implements MessageBucketManager, Initializable {
	public static final String ID = "local";

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private File m_baseDir;

	private Map<String, LocalMessageBucket> m_buckets = new HashMap<String, LocalMessageBucket>();

	public void archive(long startTime) throws IOException {
		String path = m_pathBuilder.getPath(new Date(startTime), "");
		List<String> keys = new ArrayList<String>();

		synchronized (m_buckets) {
			for (String key : m_buckets.keySet()) {
				if (key.startsWith(path)) {
					keys.add(key);
				}
			}

			for (String key : keys) {
				LocalMessageBucket bucket = m_buckets.remove(key);

				bucket.close();
				bucket.archive();
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
	public void initialize() throws InitializationException {
		if (m_baseDir == null) {
			m_baseDir = new File(m_configManager.getHdfsLocalBaseDir("dump"));
		}

		Threads.forGroup("Cat").start(new IdleChecker());
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
			final String key = "-" + id.getDomain() + "-";
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
					MessageTree tree = bucket.findById(messageId);

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
		} finally {
			t.complete();
		}
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	public DumpTreeItem getStoreMeta(MessageTree tree) throws IOException {
		DumpTreeItem item = new DumpTreeItem();

		MessageId messageId = MessageId.parse(tree.getMessageId());
		int index = messageId.getIndex();
		
		String fileName = getBucketName(messageId, tree);
		ChannelBuffer buf = getChannelBuf(index,fileName, tree);
		
		// get rid of length
		buf.readInt();

		int size = buf.readableBytes();
		byte[] data = new byte[size];

		buf.readBytes(data);
		
		item.setFileName(fileName).setIndex(index).setSize(size).setBytes(data);
		return item;
	}

	private String getBucketName(MessageId id, MessageTree tree) throws IOException {
		// <callee domain> - <caller domain> - <callee ip>
		String name = tree.getDomain() + "-" + id.getDomain() + "-" + tree.getIpAddress();
		String dataFile = m_pathBuilder.getPath(new Date(id.getTimestamp()), name);
		LocalMessageBucket bucket = m_buckets.get(dataFile);

		if (bucket == null) {
			bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
			bucket.setBaseDir(m_baseDir);
			bucket.initialize(dataFile);
			m_buckets.put(dataFile, bucket);
		}
		return dataFile;
	}

	private ChannelBuffer getChannelBuf(int index,String fileName, MessageTree tree) throws IOException {
		LocalMessageBucket bucket = m_buckets.get(fileName);
		return bucket.getChannelBuf(tree);
	}

	public void storeMessage(DumpTreeItem item) throws IOException{
		LocalMessageBucket bucket = m_buckets.get(item.getFileName());
		bucket.storeChannelBuf(item.getIndex(), item.getSize(),item.getBytes());
	}

	@Override
	public void storeMessage(MessageTree tree) throws IOException {
		MessageId id = MessageId.parse(tree.getMessageId());
		// <callee domain> - <caller domain> - <callee ip>
		String name = tree.getDomain() + "-" + id.getDomain() + "-" + tree.getIpAddress();
		String dataFile = m_pathBuilder.getPath(new Date(id.getTimestamp()), name);
		LocalMessageBucket bucket = m_buckets.get(dataFile);

		if (bucket == null) {
			bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
			bucket.setBaseDir(m_baseDir);
			bucket.initialize(dataFile);
			m_buckets.put(dataFile, bucket);
		}

		bucket.store(tree);
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
					} catch (IOException e) {
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

}
