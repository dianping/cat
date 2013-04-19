package com.dianping.cat.consumer.dump;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private DumpUploader m_uploader;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	@Inject
	private ServerStateManager m_serverStateManager;

	private Map<String, Integer> m_oldVersionDomains = new HashMap<String, Integer>();

	private Map<String, Integer> m_errorTimestampDomains = new HashMap<String, Integer>();

	private boolean m_localMode = true;

	private static final long HOUR = 60 * 60 * 1000L;

	private Logger m_logger;

	@Override
	public void doCheckpoint(boolean atEnd) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		t.setStatus(Message.SUCCESS);

		Threads.forGroup("Cat").start(new Threads.Task() {
			@Override
			public void run() {
				try {
					m_logger.info("Dump analyer starting archive!" + new Date(m_startTime));
					m_bucketManager.archive(m_startTime);
					m_logger.info("Dump analyer end archive!");
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			@Override
			public void shutdown() {
			}

			@Override
			public String getName() {
				return "DumpAnalyzer-Checkpoint";
			}
		});
		// wait the block dump complete
		m_logger.info("old version domains:" + m_oldVersionDomains);
		m_logger.info("Error timestamp:" + m_errorTimestampDomains);
		t.complete();
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

		MessageId id = MessageId.parse(tree.getMessageId());
		String domain = tree.getDomain();

		if (id.getVersion() == 2) {
			try {
				long time = tree.getMessage().getTimestamp();
				long fixedTime = time - time % (60 * 60 * 1000);
				long idTime = id.getTimestamp();
				long duration = fixedTime - idTime;

				if (duration == 0 || duration == HOUR || duration == -HOUR) {
					m_bucketManager.storeMessage(tree, id);
				} else {
					m_serverStateManager.addPigeonTimeError(1);

					Integer size = m_errorTimestampDomains.get(domain);

					if (size == null) {
						size = 1;
					} else {
						size++;
					}
					m_errorTimestampDomains.put(domain, size);
				}
			} catch (IOException e) {
				m_logger.error("Error when dumping to local file system, version 2!", e);
			}
		} else {
			Integer size = m_oldVersionDomains.get(domain);

			if (size == null) {
				size = 1;
			} else {
				size++;
			}
			m_oldVersionDomains.put(domain, size);
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

}
