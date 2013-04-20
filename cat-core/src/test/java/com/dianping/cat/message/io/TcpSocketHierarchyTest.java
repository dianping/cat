package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class TcpSocketHierarchyTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		TcpSocketHierarchySender sender = (TcpSocketHierarchySender) lookup(MessageSender.class, "tcp-socket-hierarchy");
		List<InetSocketAddress> addresses = getServerAddresses();
		StringBuilder result = new StringBuilder();
		ServerBootstrap bootstrap = createServerBootstrap(result);
		List<Channel> channels = new ArrayList<Channel>();

		for (InetSocketAddress address : addresses) {
			Channel channel = bootstrap.bind(address);

			channels.add(channel);
		}

		sender.setServerAddresses(addresses);
		sender.initialize();

		sender.send(new DefaultMessageTree());

		Thread.sleep(100 * 1000);
	}

	private ServerBootstrap createServerBootstrap(final StringBuilder result) {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
		      Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MockDecoder(result));
			}
		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		return bootstrap;
	}

	private List<InetSocketAddress> getServerAddresses() {
		List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();

		for (int i = 0; i < 10; i++) {
			list.add(new InetSocketAddress("localhost", 3000 + i));
		}

		return list;
	}

	static class MockDecoder extends FrameDecoder {
		private StringBuilder m_result;

		public MockDecoder(StringBuilder result) {
			m_result = result;
		}

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

			m_result.append('.');
			return buffer.readBytes(length);
		}
	}

}
