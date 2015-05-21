package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.message.storage.MessageBucketManager;

public class HdfsMessageBucketManager extends ContainerHolder implements MessageBucketManager, Initializable {
	public static final String ID = "hdfs";

	@Inject
	private FileSystemManager m_manager;

	@Inject
	private PathBuilder m_pathBuilder;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	private Map<String, HdfsMessageBucket> m_buckets = new ConcurrentHashMap<String, HdfsMessageBucket>();

	private void closeIdleBuckets() throws IOException {
		long now = System.currentTimeMillis();
		long hour = 3600 * 1000L;
		Set<String> closed = new HashSet<String>();

		for (Map.Entry<String, HdfsMessageBucket> entry : m_buckets.entrySet()) {
			HdfsMessageBucket bucket = entry.getValue();

			if (now - bucket.getLastAccessTime() >= hour) {
				try {
					bucket.close();
					closed.add(entry.getKey());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
		for (String close : closed) {
			HdfsMessageBucket bucket = m_buckets.remove(close);

			release(bucket);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isHdfsOn()) {
			Threads.forGroup("cat").start(new IdleChecker());
		}
	}

	@Override
	public MessageTree loadMessage(String messageId) {
		if (!m_serverConfigManager.isHdfsOn()) {
			return null;
		}

		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("BucketService", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);

		try {
			MessageId id = MessageId.parse(messageId);
			final String path = m_pathBuilder.getLogviewPath(new Date(id.getTimestamp()), "");
			final StringBuilder sb = new StringBuilder();
			FileSystem fs = m_manager.getFileSystem(ServerConfigManager.DUMP_DIR, sb);

			sb.append('/').append(path);

			final String key = id.getDomain() + '-' + id.getIpAddress();
			final String str = sb.toString();
			final Path basePath = new Path(str);
			final List<String> paths = new ArrayList<String>();

			fs.listStatus(basePath, new PathFilter() {
				@Override
				public boolean accept(Path p) {
					String name = p.getName();

					if (name.contains(key) && !name.endsWith(".idx")) {
						paths.add(path + name);
					}
					return false;
				}
			});

			t.addData(paths.toString());
			for (String dataFile : paths) {
				try {
					Cat.getProducer().logEvent("HDFSBucket", dataFile);
					HdfsMessageBucket bucket = m_buckets.get(dataFile);

					if (bucket == null) {
						bucket = (HdfsMessageBucket) lookup(MessageBucket.class, HdfsMessageBucket.ID);
						bucket.initialize(dataFile);
						m_buckets.put(dataFile, bucket);
					}
					if (bucket != null) {
						MessageTree tree = bucket.findById(messageId);

						if (tree != null && tree.getMessageId().equals(messageId)) {
							t.addData("path", dataFile);
							return tree;
						}
					}
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}
		} catch (IOException e) {
			t.setStatus(e);
			cat.logError(e);
		} catch (RuntimeException e) {
			t.setStatus(e);
			cat.logError(e);
			throw e;
		} finally {
			t.complete();
		}
		return null;
	}

	@Override
	public void storeMessage(MessageTree tree, MessageId id) {
		throw new UnsupportedOperationException("Not supported by HDFS!");
	}

	class IdleChecker implements Task {
		@Override
		public String getName() {
			return "HdfsMessageBucketManager-IdleChecker";
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(60 * 1000L); // 1 minute

					try {
						closeIdleBuckets();
					} catch (IOException e) {
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
}
