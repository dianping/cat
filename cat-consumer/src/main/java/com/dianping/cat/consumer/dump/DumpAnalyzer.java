package com.dianping.cat.consumer.dump;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.dump.DumpItem;
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

	private final BlockingQueue<DumpItem> m_storeQueue = new LinkedBlockingQueue<DumpItem>(10000);

	private int m_errors;

	public DumpUploader getDumpUploader() {
		return m_uploader;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd) {
			m_channelManager.closeAllChannels(m_startTime);

			try {
				m_bucketManager.archive(m_startTime);
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		String messageId = tree.getMessageId();
		MessageId id = MessageId.parse(messageId);

		if (id.getVersion() == 1) {
			if (!m_localMode) {
				try {
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
				} catch (Exception e) {
					m_logger.error("Error when dumping to local file system!", e);
				}
			}
		} else {
			try {
				DumpItem item = m_bucketManager.buildStoreMetaInfo(tree);
				boolean result = m_storeQueue.offer(item);
				
				if (!result) {
					m_errors++;

					if (m_errors == 1 || m_errors % 10000 == 0) {
						m_logger.error("Error when put dump item into queue, errors:" + m_errors);
					}
				}
				//m_bucketManager.storeMessage(tree);
			} catch (IOException e) {
				m_logger.error("Error when dumping to local file system!", e);
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();

		if (!m_localMode) {
			m_uploader.start();

			Threads.forGroup("Cat").start(new WriteMessageTree());
		}
	}

	public class WriteMessageTree implements Task {
		private int m_error = 0;

		@Override
		public void run() {
			// int i = 0;
			// long current = System.currentTimeMillis();
			while (true) {
				try {
					DumpItem item = m_storeQueue.poll(5, TimeUnit.MILLISECONDS);

					if (item != null) {
						m_bucketManager.storeMessage(item);

//						i++;
//						if (i % 10000 == 0) {
//							long l = System.currentTimeMillis() - current;
//							System.out.println("Total :" + i + " time " + l);
//							System.out.println((double) i / l);
//						}
					}
				} catch (Exception e) {
					if (m_error == 1 || m_error % 10000 == 0) {
						Cat.logError(e);
					}
				}
			}
		}

		@Override
		public String getName() {
			return "WriteMessageTree";
		}

		@Override
		public void shutdown() {
		}
	}
}
