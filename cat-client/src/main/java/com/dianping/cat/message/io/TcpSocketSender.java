package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

public class TcpSocketSender implements Task, MessageSender, LogEnabled {
	public static final String ID = "tcp-socket-sender";

	public static final int SIZE = 10000;

	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessageStatistics m_statistics;

	private MessageQueue m_queue = new DefaultMessageQueue(SIZE);

	private List<InetSocketAddress> m_serverAddresses;

	private ChannelManager m_manager;

	private Logger m_logger;

	private transient boolean m_active;

	private AtomicInteger m_errors = new AtomicInteger();

	private AtomicInteger m_attempts = new AtomicInteger();

	private boolean checkWritable(ChannelFuture future) {
		boolean isWriteable = false;

		if (future != null && future.getChannel().isOpen()) {
			if (future.getChannel().isWritable()) {
				isWriteable = true;
			} else {
				int count = m_attempts.incrementAndGet();

				if (count % 1000 == 0 || count == 1) {
					m_logger.error("Netty write buffer is full! Attempts: " + count);
				}
			}
		}

		return isWriteable;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "TcpSocketHierarchySender";
	}

	@Override
	public void initialize() {
		m_manager = new ChannelManager(m_logger, m_serverAddresses, m_queue);

		Threads.forGroup("Cat").start(this);
		Threads.forGroup("Cat").start(m_manager);
	}

	@Override
	public void run() {
		m_active = true;

		while (m_active) {
			ChannelFuture future = m_manager.getChannel();

			if (checkWritable(future)) {
				try {
					MessageTree tree = m_queue.poll();

					if (tree != null) {
						sendInternal(tree);
						tree.setMessage(null);
					}
				} catch (Throwable t) {
					m_logger.error("Error when sending message over TCP socket!", t);
				}
			} else {
				try {
					Thread.sleep(5);
				} catch (Exception e) {
					// ignore it
					m_active = false;
				}
			}
		}
	}

	@Override
	public void send(MessageTree tree) {
		boolean result = m_queue.offer(tree);

		if (!result) {
			if (m_statistics != null) {
				m_statistics.onOverflowed(tree);
			}

			int count = m_errors.incrementAndGet();

			if (count % 1000 == 0 || count == 1) {
				m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
			}
		}
	}

	private void sendInternal(MessageTree tree) {
		ChannelFuture future = m_manager.getChannel();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(10 * 1024); // 10K

		m_codec.encode(tree, buf);

		int size = buf.readableBytes();

		future.getChannel().write(buf);

		if (m_statistics != null) {
			m_statistics.onBytes(size);
		}
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}

	public void setServerAddresses(List<InetSocketAddress> serverAddresses) {
		m_serverAddresses = serverAddresses;
	}

	@Override
	public void shutdown() {
		m_active = false;
		m_manager.shutdown();
	}

}
