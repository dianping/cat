package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
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

	private volatile int m_error = -1;

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
		m_manager = new ChannelManager(m_logger, m_serverAddresses);

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

	public class ChannelManager implements Task {
		private List<InetSocketAddress> m_serverAddresses;

		private ClientBootstrap m_bootstrap;

		private ChannelFuture m_activeFuture;

		private Logger m_logger;

		private ChannelFuture m_lastFuture;

		private boolean m_active = true;

		private int m_activeIndex = -1;

		private int m_retriedTimes = 0;

		private AtomicInteger m_reconnects = new AtomicInteger(99);

		public ChannelManager(Logger logger, List<InetSocketAddress> serverAddresses) {
			int len = serverAddresses.size();

			m_logger = logger;
			m_serverAddresses = serverAddresses;

			ExecutorService bossExecutor = Threads.forPool().getFixedThreadPool("Cat-TcpSocketSender-Boss", 10);
			ExecutorService workerExecutor = Threads.forPool().getFixedThreadPool("Cat-TcpSocketSender-Worker", 10);
			ChannelFactory factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
			ClientBootstrap bootstrap = new ClientBootstrap(factory);

			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				@Override
				public ChannelPipeline getPipeline() {
					return Channels.pipeline(new ExceptionHandler());
				}
			});

			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);

			m_bootstrap = bootstrap;

			for (int i = 0; i < len; i++) {
				ChannelFuture future = createChannel(i);

				if (future != null) {
					m_activeFuture = future;
					m_activeIndex = i;
					break;
				}
			}
		}

		private ChannelFuture createChannel(int index) {
			InetSocketAddress address = m_serverAddresses.get(index);
			ChannelFuture future = null;

			try {
				future = m_bootstrap.connect(address);
				future.awaitUninterruptibly(100, TimeUnit.MILLISECONDS); // 100 ms

				if (!future.isSuccess()) {
					int count = m_reconnects.incrementAndGet();

					if (count % 100 == 0) {
						m_logger.error("Error when try to connecting to " + address + ", message: " + future.getCause());
					}
					future.getChannel().close();
				} else {
					m_logger.info("Connected to CAT server at " + address);
					return future;
				}
			} catch (Throwable e) {
				m_logger.error("Error when connect server " + address.getAddress(), e);

				if (future != null) {
					future.getChannel().close();
				}
			}
			return null;
		}

		public ChannelFuture getChannel() {
			return m_activeFuture;
		}

		@Override
		public String getName() {
			return "TcpSocketSender-ChannelManager";
		}

		private boolean isChannelStalled() {
			m_retriedTimes++;
			int size = m_queue.size();
			boolean stalled = m_activeFuture != null && size >= SIZE - 1;

			if (stalled) {
				if (m_retriedTimes >= 5) {
					m_retriedTimes = 0;
					m_logger.info("need to set active future to null. queue size:" + size + ",activeIndex:" + m_activeIndex);
					return true;
				} else {
					m_logger.info("no need set active future to null due to retry time is not enough. queue size:" + size
					      + ",retriedTimes:" + m_retriedTimes + ",activeIndex:" + m_activeIndex);
					return false;
				}
			} else {
				return false;
			}
		}

		@Override
		public void run() {
			while (m_active) {
				try {
					if (isChannelStalled()) {
						m_activeFuture.getChannel().close();
						m_activeFuture = null;
						m_activeIndex = -1;
					}
					if (m_activeFuture != null && !m_activeFuture.getChannel().isOpen()) {
						m_activeFuture.getChannel().close();
						m_activeFuture = null;
						m_activeIndex = m_serverAddresses.size();
					}
					if (m_activeIndex == -1) {
						m_activeIndex = m_serverAddresses.size();
					}
					if (m_lastFuture != null && m_lastFuture != m_activeFuture) {
						m_lastFuture.getChannel().close();
						m_lastFuture = null;
					}
				} catch (Throwable e) {
					m_logger.error(e.getMessage(), e);
				}
				try {
					for (int i = 0; i < m_activeIndex; i++) {
						ChannelFuture future = createChannel(i);

						if (future != null) {
							m_lastFuture = m_activeFuture;
							m_activeFuture = future;
							m_activeIndex = i;
							break;
						}
					}
				} catch (Throwable e) {
					m_logger.error(e.getMessage(), e);
				}

				try {
					Thread.sleep(2 * 1000L); // check every 2 seconds
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}

		@Override
		public void shutdown() {
			m_active = false;
		}
	}

	private class ExceptionHandler extends SimpleChannelHandler {

		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			m_error++;
			if (m_error % 1000 == 0) {
				m_logger.warn("Channel disconnected by remote address: " + e.getChannel().getRemoteAddress());
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			m_error++;
			if (m_error % 1000 == 0) {
				m_logger.warn("Channel disconnected due to " + e.getCause());
			}
		}
	}
}
