package com.dianping.cat.consumer.dump;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private DumpChannelManager m_channelManager;

	@Inject
	private DumpUploader m_uploader;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	private boolean m_localMode = true;

	private Logger m_logger;

	private final BlockingQueue<MessageTree> m_messages = new LinkedBlockingDeque<MessageTree>(10000);

	private boolean m_writeMessagesEnd = true;

	private int m_overflow;

	@Override
	public void doCheckpoint(boolean atEnd) {
		m_writeMessagesEnd = false;
		m_bucketManager.archive(m_startTime);
		m_channelManager.closeAllChannels(m_startTime);
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

		Threads.forGroup("Cat").start(new WriteOldMessage());
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

		if (id.getVersion() == 1) {
			if (!m_localMode) {
				boolean result = m_messages.offer(tree);

				if (result == false) {
					m_overflow++;

					if (m_overflow == 1 || m_overflow % 10000 == 1) {
						m_logger.error("Error when dumping to local file system, version 2 ! overflow:" + m_overflow);
					}
				}
			}
		} else {
			try {
				m_bucketManager.storeMessage(tree);
			} catch (IOException e1) {
				m_logger.error("Error when dumping to local file system, version 2!", e1);
			}

			// String realDomain = tree.getDomain();
			// String rootDomain = id.getDomain();
			// // Store an other messagetree for mapreduce, sample for domain A invoke domain B
			// if (!realDomain.equals(rootDomain)) {
			// try {
			// String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			// long timestamp = tree.getMessage().getTimestamp();
			// String domain = tree.getDomain();
			// String path = m_builder.getMessagePath(domain + "-" + ipAddress, new Date(timestamp));
			// DumpChannel channel = m_channelManager.openChannel(path, false, m_startTime);
			// channel.write(tree.getMessageId());
			// } catch (Exception e) {
			// m_logger.error("Error when dumping to local file system!", e);
			// }
			// }
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	public class WriteOldMessage implements Task {

		private int m_errors = 0;

		@Override
		public void run() {
			while (m_writeMessagesEnd) {
				try {
					MessageTree tree = m_messages.poll(5, TimeUnit.MILLISECONDS);
					if (tree != null) {
						String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
						long timestamp = tree.getMessage().getTimestamp();
						String domain = tree.getDomain();
						String path = m_builder.getMessagePath(domain + "-" + ipAddress, new Date(timestamp));
						DumpChannel channel = m_channelManager.openChannel(path, false, m_startTime);
						int length = channel.write(tree);

						if (length <= 0) {
							m_channelManager.closeChannel(channel);

							channel = m_channelManager.openChannel(path, true, m_startTime);
							channel.write(tree);
						}
					} else {
						if (m_writeMessagesEnd == false) {
							m_logger.info("write version 1 message tree end");
							break;
						}
					}
				} catch (Exception e) {
					m_errors++;
					if (m_errors == 1 || m_errors % 1000 == 1) {
						m_logger.error("Error when dumping to local file system, version 1!", e);
					}
				}
			}
		}

		@Override
		public String getName() {
			return "Write-OldMessageTree";
		}

		@Override
		public void shutdown() {
		}
	}

}
