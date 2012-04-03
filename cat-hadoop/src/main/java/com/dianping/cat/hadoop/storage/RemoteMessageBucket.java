package com.dianping.cat.hadoop.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Logview;
import com.dianping.cat.hadoop.dal.LogviewDao;
import com.dianping.cat.hadoop.dal.LogviewEntity;
import com.dianping.cat.hadoop.hdfs.InputChannel;
import com.dianping.cat.hadoop.hdfs.InputChannelManager;
import com.dianping.cat.hadoop.hdfs.OutputChannel;
import com.dianping.cat.hadoop.hdfs.OutputChannelManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class RemoteMessageBucket implements Bucket<MessageTree>, LogEnabled {
	@Inject
	private OutputChannelManager m_outputChannelManager;

	@Inject
	private InputChannelManager m_inputChannelManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private LogviewDao m_logviewDao;

	private OutputChannel m_outputChannel;

	private String m_path;

	private MessageUploadWorker m_worker;

	private Map<String, String> m_lruCache = new LinkedHashMap<String, String>(100, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, String> eldest) {
			return size() > 100;
		}
	};

	private Logger m_logger;

	@Override
	public void close() throws IOException {
		m_outputChannelManager.closeChannel(m_outputChannel);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public MessageTree findById(String id) throws IOException {
		try {
			Logview logview = m_logviewDao.findByMessageId(id, LogviewEntity.READSET_FULL);
			MessageTree tree = readMessageTree(logview);

			return tree;
		} catch (DalException e) {
			m_logger.error(String.format("Unable to find message(%s)!", id), e);

			return null;
		}
	}

	protected MessageTree findByIdAndTag(String id, String tagName, boolean direction) throws IOException {
		String tagThread = null;
		String tagSession = null;
		String tagRequest = null;

		if (tagName.startsWith("r:")) {
			tagRequest = tagName;
		} else if (tagName.startsWith("s:")) {
			tagSession = tagName;
		} else if (tagName.startsWith("t:")) {
			tagThread = tagName;
		}

		try {
			Logview logview = m_logviewDao.findNextByMessageIdTags(id, direction, tagThread, tagSession, tagRequest,
			      LogviewEntity.READSET_FULL);
			MessageTree tree = readMessageTree(logview);

			return tree;
		} catch (DalException e) {
			String message = String.format("Unable to find next message(%s) with tag(%s) and direction(%s)!", id, tagName,
			      direction);

			m_logger.error(message, e);
			return null;
		}
	}

	@Override
	public MessageTree findNextById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, true);
	}

	@Override
	public MessageTree findPreviousById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, false);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public Collection<String> getIds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize(Class<?> type, String name, Date timestamp) throws IOException {
		String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		String logicalPath = m_pathBuilder.getMessagePath(name, timestamp);

		m_path = logicalPath + "-" + ipAddress + "-" + System.currentTimeMillis();
		m_outputChannel = m_outputChannelManager.openChannel("logview", m_path, false);
		m_worker = new MessageUploadWorker();


		m_worker.setName("MessageUploadWorker");
		m_worker.start();
	}

	protected MessageTree readMessageTree(Logview logview) throws IOException {
		InputChannel inputChannel = null;

		try {
			String path = logview.getDataPath();
			long offset = logview.getDataOffset();
			int length = logview.getDataLength();

			inputChannel = m_inputChannelManager.openChannel("logview", path);

			MessageTree tree = inputChannel.read(offset, length);

			return tree;
		} finally {
			if (inputChannel != null) {
				m_inputChannelManager.closeChannel(inputChannel);
			}
		}
	}

	@Override
	public boolean storeById(String id, MessageTree tree) throws IOException {
		String messageId = tree.getMessageId();

		if (m_lruCache.containsKey(messageId)) {
			return false;
		}

		m_lruCache.put(messageId, messageId);
		return m_worker.add(tree);
	}

	class MessageUploadWorker extends Thread {
		private BlockingQueue<MessageTree> m_queue = new LinkedBlockingQueue<MessageTree>(10000);

		private boolean m_active = true;

		public boolean add(MessageTree tree) {
			return m_queue.offer(tree);
		}

		@Override
		public void run() {
			List<MessageTree> trees = new ArrayList<MessageTree>();

			try {
				while (m_active) {
					for (int i = 0; i < 10; i++) {
						MessageTree tree = m_queue.poll();

						if (tree != null) {
							trees.add(tree);
						} else {
							break;
						}
					}

					if (trees.size() > 0) {
						uploadMessage(trees);
						trees.clear();
					} else {
						Thread.sleep(10);
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		public void shutdown() {
			m_active = true;
		}

		void uploadMessage(List<MessageTree> trees) {
			MessageProducer cat = Cat.getProducer();
			Transaction t = cat.newTransaction("Bucket", getClass().getSimpleName());
			List<Logview> logviews = new ArrayList<Logview>();

			try {
				for (MessageTree tree : trees) {
					String messageId = tree.getMessageId();

					int offset = m_outputChannel.getSize();
					int length = m_outputChannel.write(tree);

					Logview logview = m_logviewDao.createLocal();

					logview.setMessageId(messageId);
					logview.setDataPath(m_path);
					logview.setDataOffset(offset);
					logview.setDataLength(length);
					logview.setTagThread("t:" + tree.getThreadId());
					logview.setTagSession("s:" + tree.getSessionToken());
					logview.setTagRequest("r:" + messageId);
					
					logviews.add(logview);
				}

				m_logviewDao.insert(logviews.toArray(new Logview[0]));
				t.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				cat.logError(e);
				t.setStatus(e);
				e.printStackTrace();
			} finally {
				t.complete();
			}
		}
	}
}
