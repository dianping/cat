package com.dianping.cat.network;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.configuration.DefaultConfigureManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ClientTransportManagerTest extends ComponentTestCase {
	private List<Integer> m_ports = new ArrayList<>();

	@Before
	public void before() {
		context().registerComponent(ConfigureManager.class, new MockConfigureManager());
	}

	@Test
	public void testReconfigure() throws Exception {
		ClientTransportManager manager = lookup(ClientTransportManager.class);
		Server s2290 = new Server(manager, 2290);
		Server s2291 = new Server(manager, 2291);
		Server s2292 = new Server(manager, 2292);

		s2290.start();
		s2291.start();
		s2292.start();
		manager.start();

		// 2290 is in and it wins
		s2290.on();
		s2290.matches(manager);

		// 2290 is the first, then it wins
		s2291.on();
		s2292.on();
		s2290.matches(manager);

		// 2290 is stopped, so 2291 wins
		s2290.stop();
		s2291.matches(manager);

		// 2290 is started and back, so 2290 wins
		s2290.start();
		s2290.matches(manager);
		
		// 2290 is removed, then 2291 is the first and it wins
		s2290.off();
		s2291.matches(manager);

		// 2291 is removed, then 2292 is the first and it wins
		s2291.off();
		s2292.matches(manager);
	}

	private class MockConfigureManager extends DefaultConfigureManager {
		@Override
		public String getProperty(String name, String defaultValue) {
			if (ConfigureProperty.ROUTERS.equals(name)) {
				StringBuilder sb = new StringBuilder();

				for (int port : m_ports) {
					sb.append("127.0.0.1").append(':').append(port).append(';');
				}

				return sb.toString();
			} else if (ConfigureProperty.RECONNECT_INTERVAL.equals(name)) {
				return "5"; // 5 ms
			}

			return super.getProperty(name, defaultValue);
		}
	}

	private class Server {
		private ClientTransportManager m_manager;

		private int m_port;

		private ServerBootstrap m_bootstrap;

		public Server(ClientTransportManager manager, int port) {
			m_manager = manager;
			m_port = port;
		}

		private ServerBootstrap makeBootstrap() {
			ServerBootstrap bootstrap = new ServerBootstrap();

			bootstrap.group(new NioEventLoopGroup(3)).channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
				}
			});

			bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

			return bootstrap;
		}

		public void matches(ClientTransportManager manager) throws InterruptedException {
			InetSocketAddress remote = null;
			int times = 200;

			do {
				TimeUnit.MILLISECONDS.sleep(5);

				List<Channel> channels = manager.getActiveChannels();

				if (!channels.isEmpty()) {
					Channel first = channels.get(0);

					remote = (InetSocketAddress) first.remoteAddress();
				} else {
					remote = null;
				}
			} while (times-- > 0 && (remote == null || m_port != remote.getPort()));

			if (remote != null) {
				if (remote.getPort() != m_port) {
					Assert.fail(String.format("Expect connection to %s, but was %s", m_port, remote.getPort()));
				}
			} else {
				Assert.fail(String.format("Expected connection to %s, but no active channels there!", m_port));
			}
		}

		public void off() {
			m_ports.remove((Integer) m_port);

			m_manager.refresh();
		}

		public void on() {
			m_ports.add(m_port);
			Collections.sort(m_ports);

			m_manager.refresh();
		}

		public void start() throws InterruptedException {
			m_bootstrap = makeBootstrap();
			m_bootstrap.bind(m_port).sync();
		}

		public void stop() {
			m_bootstrap.config().group().shutdownGracefully();
			m_bootstrap.config().childGroup().shutdownGracefully();
		}
	}
}
