package com.dianping.cat.consumer.dump;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.lookup.annotation.Inject;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private DumpChannelManager m_channelManager;

	@Inject
	private DumpUploader m_uploader;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	private boolean m_localMode = true;

	private Logger m_logger;

	@Override
	public void doCheckpoint(boolean atEnd) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		t.setStatus(Message.SUCCESS);

		try {
			m_bucketManager.archive(m_startTime);
			m_channelManager.closeAllChannels(m_startTime);
		} catch (Exception e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return Collections.emptySet();
	}

	public DumpUploader getDumpUploader() {
		return m_uploader;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();

		if (!m_localMode) {
			m_uploader.start();
		}
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	@Override
	protected void process(MessageTree tree) {
		if (tree.getMessage() == null) {
			return;
		}

		String messageId = tree.getMessageId();
		MessageId id = MessageId.parse(messageId);

		if (id.getVersion() == 2) {
			try {
				m_bucketManager.storeMessage(tree);
			} catch (IOException e1) {
				m_logger.error("Error when dumping to local file system, version 2!", e1);
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

}
