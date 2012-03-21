package com.dianping.cat.job.storage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.job.hdfs.InputChannel;
import com.dianping.cat.job.hdfs.InputChannelManager;
import com.dianping.cat.job.hdfs.OutputChannel;
import com.dianping.cat.job.hdfs.OutputChannelManager;
import com.dianping.cat.job.sql.dal.Logview;
import com.dianping.cat.job.sql.dal.LogviewDao;
import com.dianping.cat.job.sql.dal.LogviewEntity;
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
	public void deleteAndCreate() throws IOException {
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public MessageTree findById(String id) throws IOException {
		try {
			Logview logview = m_logviewDao.findByPK(id, LogviewEntity.READSET_FULL);
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
	public Collection<String> getIdsByPrefix(String prefix) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize(Class<?> type, String name, Date timestamp) throws IOException {
		String ipAddress = InetAddress.getLocalHost().getHostAddress();
		String logicalPath = m_pathBuilder.getMessagePath(name, timestamp);

		// TODO make it lazy
		m_path = logicalPath + "-" + ipAddress + "-" + System.currentTimeMillis();
		m_outputChannel = m_outputChannelManager.openChannel(m_path, false);
	}

	protected MessageTree readMessageTree(Logview logview) throws IOException {
		InputChannel inputChannel = null;

		try {
			String path = logview.getDataPath();
			long offset = logview.getDataOffset();
			int length = logview.getDataLength();

			inputChannel = m_inputChannelManager.openChannel(path);

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

		try {
			m_logviewDao.insert(logview);
			return true;
		} catch (DalException e) {
			throw new IOException("Error when inserting into logview table!", e);
		}
	}
}
