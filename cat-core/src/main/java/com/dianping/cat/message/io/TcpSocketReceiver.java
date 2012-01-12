package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

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

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public class TcpSocketReceiver implements MessageReceiver, LogEnabled {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	@Inject
	private MessageCodec m_codec;

	private ChannelFactory m_factory;

	private ChannelGroup m_channelGroup = new DefaultChannelGroup();

	private MessageHandler m_messageHandler;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() {
		InetSocketAddress address;

		if (m_host == null) {
			address = new InetSocketAddress(m_port);
		} else {
			address = new InetSocketAddress(m_host, m_port);
		}

		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
		      Executors.newCachedThreadPool());
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

	@Override
	public void onMessage(MessageHandler handler) {
		m_messageHandler = handler;
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
		ChannelGroupFuture future = m_channelGroup.close();

		future.awaitUninterruptibly();
		m_factory.releaseExternalResources();
	}

	public class MyDecoder extends FrameDecoder {
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

			// TODO filter

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
			event.getCause().printStackTrace();

			event.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
			ChannelBuffer buf = (ChannelBuffer) event.getMessage();
			MessageTree tree = new DefaultMessageTree();

			m_codec.decode(buf, tree);
			m_messageHandler.handle(tree);
		}
	}
}
