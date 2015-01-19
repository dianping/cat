package com.dianping.cat.consumer.dump;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.core.MessagePathBuilder;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.storage.message.LocalMessageBucket;
import com.dianping.cat.storage.message.MessageBlock;

public class MessageGzip implements Task {

	private LocalMessageBucketManager m_bucketManager;

	private File m_baseDir;

	private ConcurrentHashMap<String, LocalMessageBucket> m_buckets;

	private ServerStatisticManager m_serverStateManager;

	private MessagePathBuilder m_pathBuilder;

	private BlockingQueue<MessageBlock> m_messageBlocks;

	private int m_index;

	public BlockingQueue<MessageItem> m_messageQueue;

	private int m_count = -1;

	private String m_localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	public MessageGzip(LocalMessageBucketManager manager, ConcurrentHashMap<String, LocalMessageBucket> buckets,
	      MessagePathBuilder pathBuilder, ServerStatisticManager serverStateManager,
	      BlockingQueue<MessageBlock> messageBlocks, ServerConfigManager configManager,
	      BlockingQueue<MessageItem> messageQueue, int index) {
		m_baseDir = new File(configManager.getHdfsLocalBaseDir(ServerConfigManager.DUMP_DIR));
		m_bucketManager = manager;
		m_buckets = buckets;
		m_pathBuilder = pathBuilder;
		m_serverStateManager = serverStateManager;
		m_messageBlocks = messageBlocks;
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
			String path = m_pathBuilder.getPath(new Date(id.getTimestamp()), name);
			LocalMessageBucket bucket = m_buckets.get(path);

			if (bucket == null) {
				synchronized (m_buckets) {
					bucket = m_buckets.get(path);
					if (bucket == null) {
						bucket = m_bucketManager.lookupBucket();
						bucket.setBaseDir(m_baseDir);
						bucket.initialize(path);

						LocalMessageBucket last = m_buckets.putIfAbsent(path, bucket);

						if (last != null) {
							bucket.close();

							Cat.logEvent("BucketConcurrentModify", path, Event.SUCCESS, null);
						}

						bucket = m_buckets.get(path);
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