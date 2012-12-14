package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;

import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.status.ServerStateManager;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

public class TcpSocketReceiver implements LogEnabled {
	private boolean m_active = true;

	private ChannelGroup m_channelGroup = new DefaultChannelGroup();

	@Inject
	private MessageCodec m_codec;

	private ChannelFactory m_factory;

	@Inject
	private MessageHandler m_handler;

	@Inject
	private String m_host;

	private Logger m_logger;

	@Inject
	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	private BlockingQueue<ChannelBuffer> m_queue;

	private int m_queueSize = 100000;

	private int m_error;

	private int m_process;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private ServerStateManager m_serverStateManager;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void init() {
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		InetSocketAddress address;

		m_host = m_serverConfigManager.getBindHost();
		m_port = m_serverConfigManager.getBindPort();

		if (m_host == null) {
			address = new InetSocketAddress(m_port);
		} else {
			address = new InetSocketAddress(m_host, m_port);
		}

		m_queue = new LinkedBlockingQueue<ChannelBuffer>(m_queueSize);

		ExecutorService bossExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Boss-" + address);
		ExecutorService workerExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Worker");
		ChannelFactory factory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MessageDecoder(), new MyHandler());
			}
		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(address);

		m_logger.info("CAT server started at " + address);
		m_factory = factory;
	}

	public boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}

	public void setQueueSize(int queueSize) {
		m_queueSize = queueSize;
	}

	public class DecodeMessageTask implements Task {

		private int m_index;

		public DecodeMessageTask(int index) {
			m_index = index;
		}

		@Override
		public String getName() {
			return "Message-Decode-" + m_index;
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					ChannelBuffer buf = m_queue.poll(1, TimeUnit.MILLISECONDS);

					if (buf != null) {
						try {
							buf.markReaderIndex();
							// read the size of the message
							buf.readInt();
							DefaultMessageTree tree = (DefaultMessageTree) m_codec.decode(buf);
							buf.resetReaderIndex();
							tree.setBuf(buf);
							m_handler.handle(tree);
						} catch (Throwable e) {
							buf.resetReaderIndex();

							String raw = buf.toString(0, buf.readableBytes(), Charset.forName("utf-8"));
							m_logger.error("Error when handling message! Raw buffer: " + raw, e);
						}
					}
				} catch (Exception e) {
					active = false;
				}
			}
			try {
				if (m_index == 1) {
					ChannelGroupFuture future = m_channelGroup.close();

					future.awaitUninterruptibly();
					m_factory.releaseExternalResources();
				}

			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void shutdown() {
		}

	}

	public static class MessageDecoder extends FrameDecoder {
		@Override
		/**
		 * return null means not all data is ready, so waiting for next network package.
		 */
		protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
			if (buffer.readableBytes() < 4) {
				return null;
			}

			buffer.markReaderIndex();

			int length = buffer.readInt();

			buffer.resetReaderIndex();

			if (buffer.readableBytes() < length + 4) {
				return null;
			}

			return buffer.readBytes(length + 4);
		}
	}

	class MyHandler extends SimpleChannelHandler {

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
			m_channelGroup.add(event.getChannel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) {
			m_logger.warn(event.getChannel().toString(), event.getCause());

			event.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
			ChannelBuffer buf = (ChannelBuffer) event.getMessage();
			boolean result = m_queue.offer(buf);

			if (result == false) {
				m_error++;
				if (m_error % CatConstants.ERROR_COUNT == 0) {
					m_serverStateManager.addMessageTotalLoss(CatConstants.ERROR_COUNT);
					m_logger.warn("The server can't process the tree! overflow : " + m_error);
				}
			} else {
				m_process++;
				int flag = m_process % CatConstants.SUCCESS_COUNT;
				
				if (flag == 0) {
					m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);

					if (flag % CatConstants.SUCCESS_COUNT == 0) {
						m_logger.info("The server processes message number " + m_process);
					}
				}
			}
		}
	}

	public void startEncoderThreads(int threadSize) {
		for (int i = 0; i < threadSize; i++) {
			DecodeMessageTask messageDecoder = new DecodeMessageTask(i);

			Threads.forGroup("Cat").start(messageDecoder);
		}
	}
}
