package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
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

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class TcpSocketSender implements Task, MessageSender, LogEnabled {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessageQueue m_queue;

	@Inject
	private MessageStatistics m_statistics;

	private InetSocketAddress m_serverAddress;

	private ChannelFactory m_factory;

	private ChannelFuture m_future;

	private ClientBootstrap m_bootstrap;

	private int m_reconnectPeriod = 5000; // every 5 seconds

	private long m_lastReconnectTime;

	private Logger m_logger;

	private transient boolean m_active;

	private AtomicInteger m_errors = new AtomicInteger();

	private AtomicInteger m_attempts = new AtomicInteger();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() {
		if (m_serverAddress == null) {
			throw new RuntimeException("No server address was configured for TcpSocketSender!");
		}

		ExecutorService bossExecutor = Threads.forPool().getFixedThreadPool(
		      "Cat-TcpSocketSender-Boss-" + m_serverAddress, 10);
		ExecutorService workerExecutor = Threads.forPool().getFixedThreadPool("Cat-TcpSocketSender-Worker", 10);
		ChannelFactory factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
		ClientBootstrap bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MyHandler());
			}
		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		ChannelFuture future = bootstrap.connect(m_serverAddress);

		future.awaitUninterruptibly();

		if (!future.isSuccess()) {
			m_logger.error("Error when connecting to " + m_serverAddress, future.getCause());
		} else {
			m_factory = factory;
			m_future = future;
			m_logger.info("Connected to CAT server at " + m_serverAddress);
		}

		m_bootstrap = bootstrap;
		Threads.forGroup("Cat").start(this);
	}

	public void reconnect() {
		long now = System.currentTimeMillis();

		if (m_lastReconnectTime > 0 && m_lastReconnectTime + m_reconnectPeriod > now) {
			return;
		}

		m_lastReconnectTime = now;

		ChannelFuture future = m_bootstrap.connect(m_serverAddress);

		future.awaitUninterruptibly();

		if (!future.isSuccess()) {
			//m_logger.error("Error when reconnecting to " + m_serverAddress, future.getCause());
		} else {
			m_future = future;
			m_logger.info("Reconnected to CAT server at " + m_serverAddress);
		}
	}

	@Override
	public void run() {
		m_active = true;

		while (m_active) {
			try {
				if (checkWritable()) {
					MessageTree tree = m_queue.poll();

					if (tree != null) {
						sendInternal(tree);

						tree.setMessage(null);
					}
				} else {
					try {
						Thread.sleep(2);
					} catch (Exception e) {
						break;
					}
					
					if (m_future == null || !m_future.getChannel().isOpen()) {
						reconnect();
					}
				}
			} catch (Throwable t) {
				m_logger.error("Error when sending message over TCP socket!", t);
			}
		}

		if (m_future != null) {
			m_future.getChannel().getCloseFuture().awaitUninterruptibly();
		}

		if (m_factory != null) {
			m_factory.releaseExternalResources();
		}
	}

	boolean checkWritable() {
		boolean isWriteable = false;

		if (m_future != null && m_future.getChannel().isOpen()) {
			if (m_future.getChannel().isWritable()) {
				isWriteable = true;
			} else {
				int count = m_attempts.incrementAndGet();

				if (count % 100 == 0) {
					m_logger.error("Can't send message to cat-server due to Netty write buffer full! Count: " + count);
				}
			}
		}

		return isWriteable;
	}

	@Override
	public void send(MessageTree tree) {
		boolean result = m_queue.offer(tree);

		if (!result) {
			if (m_statistics != null) {
				m_statistics.onOverflowed(tree);
			}

			int count = m_errors.incrementAndGet();

			if (count % 100 == 0) {
				m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
			}
		}
	}

	private void sendInternal(MessageTree tree) {
		if (m_future == null || !m_future.getChannel().isOpen()) {
			reconnect();
		}

		if (m_future != null && m_future.getChannel().isOpen()) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8 * 1024); // 8K

			m_codec.encode(tree, buf);

			int size = buf.readableBytes();

			m_future.getChannel().write(buf);

			if (m_statistics != null) {
				m_statistics.onBytes(size);
			}
		}
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}

	public void setReconnectPeriod(int reconnectPeriod) {
		m_reconnectPeriod = reconnectPeriod;
	}

	public void setServerAddress(InetSocketAddress serverAddress) {
		m_serverAddress = serverAddress;
	}

	@Override
	public void shutdown() {
		m_active = false;
	}

	class MyHandler extends SimpleChannelHandler {
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			m_logger.warn("Channel disconnected by remote address: " + e.getChannel().getRemoteAddress());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			e.getChannel().close();
		}
	}

	@Override
	public String getName() {
		return "TcpSocketSender";
	}
}
