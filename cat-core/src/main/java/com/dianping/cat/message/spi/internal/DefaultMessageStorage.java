package com.dianping.cat.message.spi.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultMessageStorage implements MessageStorage, Initializable, Disposable, LogEnabled {
	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private MessageCodec m_codec;

	private WriteJob m_job = new WriteJob();

	private Logger m_logger;

	@Override
	public void dispose() {
		m_job.shutdown();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		Thread thread = new Thread(m_job);

		thread.setName("Storage write Job");
		thread.start();
	}

	@Override
	public String getPath(MessageTree tree) {
		String path = m_builder.getLogViewPath(tree);

		return path;
	}

	@Override
	public String store(MessageTree tree) {
		String path = m_builder.getLogViewPath(tree);

		m_job.append(tree);
		return path;
	}

	class WriteJob implements Runnable {
		private BlockingQueue<MessageTree> m_queue = new LinkedBlockingQueue<MessageTree>();

		private boolean m_active = true;

		public void append(MessageTree tree) {
			try {
				m_queue.offer(tree);
			} catch (Exception e) {
				m_logger.warn("Error when adding job to queue.", e);
			}
		}

		private void handle(MessageTree tree) {
			String path = m_builder.getLogViewPath(tree);
			File file = new File(m_builder.getLogViewBaseDir(), path);

			if (!file.exists()) {
				ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
				FileOutputStream fos = null;

				file.getParentFile().mkdirs();

				try {
					fos = new FileOutputStream(file);
					m_codec.encode(tree, buf);

					int length = buf.readInt();

					buf.getBytes(buf.readerIndex(), fos, length);
				} catch (IOException e) {
					m_logger.error(String.format("Error when writing to file(%s)!", file), e);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							// ignore it
						}
					}
				}
			}
		}

		@Override
		public void run() {
			try {
				while (m_active) {
					MessageTree tree = m_queue.poll(1000 * 1000L, TimeUnit.NANOSECONDS);

					if (tree != null) {
						handle(tree);
					}
				}

				// process the remaining job in the queue
				while (!m_active) {
					MessageTree tree = m_queue.poll();

					if (tree != null) {
						handle(tree);
					} else {
						break;
					}
				}
			} catch (Exception e) {
				m_logger.warn("Error when writing message to local file system.", e);
			}
		}

		public void shutdown() {
			m_active = false;
		}
	}
}
