package com.dianping.cat.consumer.dump;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements LogEnabled {
	public static final String ID = "dump";

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private Map<String, Integer> m_oldVersionDomains = new HashMap<String, Integer>();

	private Map<String, Integer> m_errorTimestampDomains = new HashMap<String, Integer>();

	private Logger m_logger;

	private void checkpointAsyc(final long startTime) {
		Threads.forGroup("cat").start(new Threads.Task() {
			@Override
			public String getName() {
				return "DumpAnalyzer-Checkpoint";
			}

			@Override
			public void run() {
				try {
					m_bucketManager.archive(startTime);
					m_logger.info("Dump analyzer checkpoint is completed!");
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			@Override
			public void shutdown() {
			}
		});
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		try {
			long startTime = getStartTime();

			checkpointAsyc(startTime);
			m_logger.info("Old version domains:" + m_oldVersionDomains);
			m_logger.info("Error timestamp:" + m_errorTimestampDomains);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Map<String, Integer> getErrorTimestampDomains() {
		return m_errorTimestampDomains;
	}

	public Map<String, Integer> getOldVersionDomains() {
		return m_oldVersionDomains;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
   protected void loadReports() {
		//do nothing
   }

	@Override
	protected void process(MessageTree tree) {
		MessageId messageId = MessageId.parse(tree.getMessageId());
		String domain = tree.getDomain();

		if ("PhoenixAgent".equals(domain)) {
			return;
		}
		if (messageId.getVersion() == 2) {
			long time = tree.getMessage().getTimestamp();
			long fixedTime = time - time % (60 * 60 * 1000);
			long idTime = messageId.getTimestamp();
			long duration = fixedTime - idTime;

			if (duration == 0 || duration == ONE_HOUR || duration == -ONE_HOUR) {
				m_bucketManager.storeMessage(tree, messageId);
			} else {
				m_serverStateManager.addPigeonTimeError(1);

				Integer size = m_errorTimestampDomains.get(domain);

				if (size == null) {
					m_errorTimestampDomains.put(domain, 1);
				} else {
					m_errorTimestampDomains.put(domain, size + 1);
				}
			}
		} else {
			Integer size = m_oldVersionDomains.get(domain);

			if (size == null) {
				m_oldVersionDomains.put(domain, 1);
			} else {
				m_oldVersionDomains.put(domain, size + 1);
			}
		}
	}

	public void setBucketManager(LocalMessageBucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		m_serverStateManager = serverStateManager;
	}

}
