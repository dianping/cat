package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
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

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Threads;
import com.site.lookup.annotation.Inject;

public class TcpSocketReceiver implements MessageReceiver, LogEnabled {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	@Inject
	private MessageCodec m_codec;

	private BlockingQueue<ChannelBuffer> m_queue;

	private ChannelFactory m_factory;

	private ChannelGroup m_channelGroup = new DefaultChannelGroup();

	private boolean m_active = true;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() {
		// disable thread renaming of Netty
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		InetSocketAddress address;

		if (m_host == null) {
			address = new InetSocketAddress(m_port);
		} else {
			address = new InetSocketAddress(m_host, m_port);
		}

		m_queue = new LinkedBlockingQueue<ChannelBuffer>();

		ExecutorService bossExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Boss-" + address);
		ExecutorService workerExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Worker");
		ChannelFactory factory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MyDecoder(), new MyHandler());
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

	@Override
	public void onMessage(MessageHandler handler) {
		try {
			while (true) {
				ChannelBuffer buf = m_queue.poll(1, TimeUnit.MILLISECONDS);

				if (buf != null) {
					MessageTree tree = m_codec.decode(buf);

					handler.handle(tree);
				} else if (!isActive()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		ChannelGroupFuture future = m_channelGroup.close();

		future.awaitUninterruptibly();
		m_factory.releaseExternalResources();
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}

	public void setHost(String host) {
		m_host = host;
	}

	public void setPort(int port) {
		m_port = port;
	}

	@Override
	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}

	public static class MyDecoder extends FrameDecoder {
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

			if (buffer.readableBytes() < length) {
				buffer.resetReaderIndex();
				return null;
			}

			return buffer.readBytes(length);
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

			m_queue.offer(buf);
		}
	}
}
