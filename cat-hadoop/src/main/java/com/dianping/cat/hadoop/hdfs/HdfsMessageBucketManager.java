package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.dump.MessageBucket;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class HdfsMessageBucketManager extends ContainerHolder implements MessageBucketManager, Initializable {
	public static final String ID = "hdfs";

	@Inject
	private FileSystemManager m_manager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private Map<String, HdfsMessageBucket> m_buckets = new HashMap<String, HdfsMessageBucket>();

	@Override
	public void close() throws IOException {
		for (HdfsMessageBucket bucket : m_buckets.values()) {
			bucket.close();
		}
	}

	void closeIdleBuckets() throws IOException {
		long now = System.currentTimeMillis();
		long hour = 3600 * 1000L;

		for (HdfsMessageBucket bucket : m_buckets.values()) {
			if (now - bucket.getLastAccessTime() >= hour) {
				bucket.close();
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
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
			final StringBuilder sb = new StringBuilder();
			FileSystem fs = m_manager.getFileSystem("dump", sb);

			sb.append('/').append(path);

			final String key = "-" + id.getDomain() + "-";
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

			for (String dataFile : paths) {
				HdfsMessageBucket bucket = m_buckets.get(dataFile);

				if (bucket == null) {
					bucket = (HdfsMessageBucket) lookup(MessageBucket.class, HdfsMessageBucket.ID);
					bucket.initialize(dataFile);
					m_buckets.put(dataFile, bucket);
				}

				if (bucket != null) {
					return bucket.findById(messageId);
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

	@Override
	public void storeMessage(MessageTree tree) throws IOException {
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
						e.printStackTrace();
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
