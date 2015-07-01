package com.dianping.cat.consumer.dump;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements LogEnabled {
	public static final String ID = "dump";

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

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
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public ReportManager<?> getReportManager() {
		return null;
	}

	@Override
	protected void loadReports() {
		// do nothing
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		if ("PhoenixAgent".equals(domain)) {
			return;
		} else {
			MessageId messageId = MessageId.parse(tree.getMessageId());

			if (messageId.getVersion() == 2) {
				long time = tree.getMessage().getTimestamp();
				long fixedTime = time - time % (TimeHelper.ONE_HOUR);
				long idTime = messageId.getTimestamp();
				long duration = fixedTime - idTime;

				if (duration == 0 || duration == ONE_HOUR || duration == -ONE_HOUR) {
					m_bucketManager.storeMessage(tree, messageId);
				} else {
					m_serverStateManager.addPigeonTimeError(1);
				}
			}
		}
	}

	public void setBucketManager(LocalMessageBucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		m_serverStateManager = serverStateManager;
	}

	@Override
	public int getAnanlyzerCount() {
		return 2;
	}

}
