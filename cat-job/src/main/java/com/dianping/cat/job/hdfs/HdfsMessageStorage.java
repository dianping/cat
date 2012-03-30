package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.LocalIP;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class HdfsMessageStorage implements MessageStorage, Initializable, Disposable, LogEnabled {
	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private OutputChannelManager m_manager;

	private WriteJob m_job;

	private Thread m_thread;

	private Logger m_logger;

	@Override
	public void dispose() {
		m_job.shutdown();

		try {
			m_thread.join();
		} catch (InterruptedException e) {
			// ignore it
		}
		
		this.m_manager.closeAllChannels();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_job = new WriteJob();

		Thread thread = new Thread(m_job);

		thread.setName("HdfsMessageStorage-WriteJob");
		thread.start();

		m_thread = thread;
	}

	@Override
	public String getPath(MessageTree tree) {
		String path = m_builder.getLogViewPath(tree.getMessageId());

		return path;
	}

	@Override
	public String store(MessageTree tree, String... tags) {
		m_job.append(tree);

		// Not available
		return null;
	}

	class WriteJob implements Runnable {
		private BlockingQueue<MessageTree> m_queue = new LinkedBlockingQueue<MessageTree>();

		private volatile boolean m_active = true;

		public void append(MessageTree tree) {
			try {
				m_queue.offer(tree);
			} catch (Exception e) {
				m_logger.warn("Error when adding job to queue.", e);
			}
		}

		private void handle(MessageTree tree) {
			try {
				String tmp = m_builder.getMessagePath(tree.getDomain(), new Date(tree.getMessage().getTimestamp()));
				String ipAddress = LocalIP.getAddress();
				String path = tmp + "-" + ipAddress;
				OutputChannel channel = m_manager.openChannel("dump", path, false);
				int length = channel.write(tree);

				if (length <= 0) {
					m_manager.closeChannel(channel);

					channel = m_manager.openChannel("dump", path, true);
					channel.write(tree);
				}
			} catch (IOException e) {
				m_logger.error("Error when writing to HDFS!", e);
			}
		}

		private boolean isActive() {
			synchronized (this) {
				return m_active;
			}
		}

		@Override
		public void run() {
			long lastCheckedTime = System.currentTimeMillis();

			try {
				while (isActive()) {
					MessageTree tree = m_queue.poll(1000 * 1000L, TimeUnit.NANOSECONDS);

					if (tree != null) {
						handle(tree);
					}

					// check connection timeout and close it
					if (System.currentTimeMillis() - lastCheckedTime >= 5 * 1000) {
						lastCheckedTime = System.currentTimeMillis();
						m_manager.cleanupChannels();
					}
				}

				// process the remaining job in the queue
				while (!isActive()) {
					MessageTree tree = m_queue.poll();

					if (tree != null) {
						handle(tree);
					} else {
						break;
					}
				}
			} catch (Exception e) {
				m_logger.warn("Error when dumping message to HDFS.", e);
			}

			m_manager.closeAllChannels();
		}

		public void shutdown() {
			synchronized (this) {
				m_active = false;
			}
		}
	}

	public MessageTree get(String messageId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageTree next(String messageId, String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageTree previous(String messageId, String tag) {
		throw new UnsupportedOperationException();
	}
}
