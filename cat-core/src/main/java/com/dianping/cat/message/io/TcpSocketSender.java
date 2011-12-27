package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.codec.MessageCodec;
import com.site.lookup.annotation.Inject;

public class TcpSocketSender implements MessageSender {
	@Inject
	private String m_host;

	@Inject
	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	@Inject
	private MessageCodec m_codec;

	private ChannelFactory m_factory;

	private ChannelFuture m_future;

	@Override
	public void initialize() {
		if (m_host == null) {
			throw new RuntimeException("No host was configured for TcpSocketSender!");
		}

		InetSocketAddress address = new InetSocketAddress(m_host, m_port);
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
		      Executors.newCachedThreadPool());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MyHandler());
			}
		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		ChannelFuture future = bootstrap.connect(address);

		future.awaitUninterruptibly();

		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
		} else {
			m_factory = factory;
			m_future = future;
		}
	}

	@Override
	public void send(Message message) {
		byte[] data = m_codec.encode(message);
		Channel channel = m_future.getChannel();
		ChannelBufferFactory factory = channel.getConfig().getBufferFactory();
		ChannelBuffer buf = factory.getBuffer(data.length + 4);

		buf.writeInt(data.length);
		buf.writeBytes(data);
		channel.write(buf);
	}

	public void setHost(String host) {
		m_host = host;
	}

	public void setPort(int port) {
		m_port = port;
	}

	@Override
	public void shutdown() {
		m_future.getChannel().getCloseFuture().awaitUninterruptibly();
		m_factory.releaseExternalResources();
	}

	class MyHandler extends SimpleChannelHandler {
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			e.getCause().printStackTrace();

			e.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();

			while (buf.readable()) {
				System.out.println((char) buf.readByte());
				System.out.flush();
			}
		}
	}
}
