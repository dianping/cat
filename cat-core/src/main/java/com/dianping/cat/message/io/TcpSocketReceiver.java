package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

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

import com.dianping.cat.message.Message;
import com.dianping.cat.message.codec.MessageCodec;
import com.dianping.cat.message.handler.MessageHandler;
import com.site.lookup.annotation.Inject;

public class TcpSocketReceiver implements MessageReceiver {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	@Inject
	private MessageCodec m_codec;

	private ChannelFactory m_factory;

	private ChannelGroup m_channelGroup = new DefaultChannelGroup();

	private MessageHandler m_messageHandler;

	void handleMessage(byte[] data) {
		Message message = m_codec.decode(data);

		m_messageHandler.handle(message);
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

		m_factory = factory;
	}

	@Override
	public void onMessage(MessageHandler handler) {
		m_messageHandler = handler;
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
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			m_channelGroup.add(e.getChannel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			e.getCause().printStackTrace();

			e.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();
			int length = buf.readableBytes();
			byte[] data = new byte[length];

			buf.readBytes(data);
			handleMessage(data);
		}
	}
}
