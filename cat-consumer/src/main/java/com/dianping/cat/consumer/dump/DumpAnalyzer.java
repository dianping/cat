package com.dianping.cat.consumer.dump;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements LogEnabled {
	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private DumpChannelManager m_manager;

	private long m_extraTime;

	private long m_startTime;

	private long m_duration;

	private Logger m_logger;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd) {
			m_manager.closeAllChannels();
			// TODO upload to remote HDFS
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

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
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

		try {
			String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			long timestamp = tree.getMessage().getTimestamp();
			String domain = tree.getDomain();
			String path = m_builder.getMessagePath(domain + "-" + ipAddress, new Date(timestamp));
			DumpChannel channel = m_manager.openChannel(path, false);
			int length = channel.write(tree);

			if (length <= 0) {
				m_manager.closeChannel(channel);

				channel = m_manager.openChannel(path, true);
				channel.write(tree);
			}
		} catch (Exception e) {
			m_logger.error("Error when dumping to local file system!", e);
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}
}
