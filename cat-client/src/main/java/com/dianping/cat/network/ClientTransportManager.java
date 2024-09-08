package com.dianping.cat.network;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.support.Splitters;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

// Component
public class ClientTransportManager implements Initializable, LogEnabled {
	// Inject
	private ConfigureManager m_configureManager;

	// Inject
	private MessageTransporter m_transporter;

	private Bootstrap m_bootstrap;

	private ChannelManager m_channelManager;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	// for test only
	List<Channel> getActiveChannels() {
		return m_transporter.getActiveChannels();
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
		m_transporter = ctx.lookup(MessageTransporter.class);

		m_bootstrap = makeBootstrap();
	}

	private boolean isEpollSupported() {
		boolean epollEnabled = m_configureManager.getBooleanProperty(ConfigureProperty.EPOLL_ENABLED, true);

		if (epollEnabled) {
			String os = System.getProperty("os.name");

			if (os != null) {
				return os.toLowerCase().startsWith("linux");
			}
		}

		return false;
	}

	private Bootstrap makeBootstrap() {
		Bootstrap bootstrap = new Bootstrap();
		ThreadFactory factory = new DaemonThreadFactory("Cat-" + getClass().getSimpleName());
		int workThreads = m_configureManager.getIntProperty(ConfigureProperty.NETWORK_WORKER_THREADS, 4);

		if (isEpollSupported()) {
			EpollEventLoopGroup worker = new EpollEventLoopGroup(workThreads, factory);

			bootstrap.group(worker).channel(EpollSocketChannel.class);
		} else {
			NioEventLoopGroup worker = new NioEventLoopGroup(workThreads, factory);

			bootstrap.group(worker).channel(NioSocketChannel.class);
		}

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				pipeline.addLast(m_transporter.getClass().getSimpleName(), m_transporter);
			}
		});

		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		return bootstrap;
	}

	// for test only
	void refresh() {
		m_channelManager.refresh();
	}

	public void start() {
		m_channelManager = new ChannelManager();

		Threads.forGroup("Cat").start(m_channelManager);
		Threads.forGroup("Cat").start(m_transporter);
	}

	public void stop() {
		m_bootstrap.config().group().shutdownGracefully();
	}

	private class ChannelManager implements Task {
		private List<InetSocketAddress> m_endpoints = new ArrayList<>();

		private List<InetSocketAddress> m_unreachable = new ArrayList<>();

		private LogLimiter m_limiter = new LogLimiter(1);

		private AtomicBoolean m_enabled = new AtomicBoolean(true);

		private CountDownLatch m_latch = new CountDownLatch(1);

		private void connectOne(List<InetSocketAddress> endpoints, List<InetSocketAddress> unreachable) {
			for (InetSocketAddress endpoint : endpoints) {
				ChannelFuture future = m_bootstrap.connect(endpoint);

				future.awaitUninterruptibly(100, TimeUnit.MILLISECONDS); // wait 100 ms

				if (future.isSuccess()) {
					m_limiter.reset();
					break;
				} else {
					if (m_limiter.isEligible()) {
						m_logger.warn("Unable to connect to CAT server %s, times: %s", endpoint, m_limiter.getCurrent());
					}

					unreachable.add(endpoint);
				}
			}
		}

		private List<InetSocketAddress> getEndpoints() {
			List<InetSocketAddress> endpoints = new ArrayList<InetSocketAddress>();
			String routes = m_configureManager.getProperty(ConfigureProperty.ROUTERS, null);

			if (routes != null) {
				List<String> servers = Splitters.by(';').trim().noEmptyItem().split(routes);

				for (String server : servers) {
					try {
						List<String> parts = Splitters.by(':').trim().split(server);
						String ip = parts.size() > 0 ? parts.get(0) : "";
						String port = parts.size() > 1 ? parts.get(1) : "2280";

						endpoints.add(new InetSocketAddress(ip, Integer.parseInt(port)));
					} catch (Exception e) {
						// ignore it
					}
				}
			} else {
				for (Server server : m_configureManager.getServers()) {
					endpoints.add(new InetSocketAddress(server.getIp(), server.getPort()));
				}
			}

			return endpoints;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		private void refresh() {
			List<Channel> channels = m_transporter.getActiveChannels();
			List<InetSocketAddress> endpoints = getEndpoints();

			if (channels.isEmpty()) { // no connection yet
				m_unreachable.clear();
				connectOne(endpoints, m_unreachable);
				m_endpoints = endpoints;
			} else { // has connection
				if (!m_endpoints.equals(endpoints)) { // configure changed
					if (m_unreachable.isEmpty()) { // first one is active
						if (m_endpoints.size() > 0 && endpoints.size() > 0) {
							if (!m_endpoints.get(0).equals(endpoints.get(0))) { // first one is changed
								m_unreachable.clear();
								connectOne(endpoints, m_unreachable);
								channels.get(0).close(); // disconnect the first one
							}
						}
					}
					m_endpoints = endpoints;
				} else if (!m_unreachable.isEmpty()) { // first one was unavailable
					int index = 0;
					boolean reached = false;

					for (InetSocketAddress endpoint : m_unreachable) {
						ChannelFuture future = m_bootstrap.connect(endpoint);

						future.awaitUninterruptibly(100, TimeUnit.MILLISECONDS); // wait 100 ms

						if (future.isSuccess()) {
							reached = true;
							break;
						} else {
							index++;
						}
					}

					if (reached) {
						for (int i = m_unreachable.size() - 1; i >= index; i--) {
							m_unreachable.remove(i);
						}

						channels.get(0).close(); // disconnect it
					}
				}
			}
		}

		@Override
		public void run() {
			long lastCheckTime = 0;
			long checkInterval = m_configureManager.getLongProperty(ConfigureProperty.RECONNECT_INTERVAL, 2000L);

			try {
				while (m_enabled.get()) {
					long now = System.currentTimeMillis();

					if (now - lastCheckTime >= checkInterval) {
						refresh();

						lastCheckTime = now;
					}

					TimeUnit.MILLISECONDS.sleep(10);
				}
			} catch (InterruptedException e) {
				// ignore it
			} finally {
				m_latch.countDown();
			}
		}

		@Override
		public void shutdown() {
			m_enabled.set(false);

			try {
				m_latch.await();
			} catch (InterruptedException e) {
				// ignore it
			}
		}
	}

	private static class LogLimiter {
		private int m_last2;

		private int m_last1;

		private int m_current;

		private int m_watermark;

		public LogLimiter(int watermark) {
			m_watermark = watermark;

			reset();
		}

		public int getCurrent() {
			return m_current;
		}

		public boolean isEligible() {
			int total = m_last1 + m_last2;

			m_current++;

			if (m_current >= total) {
				m_last2 = m_last1;
				m_last1 = total;

				return m_current > m_watermark;
			} else {
				return false;
			}
		}

		public void reset() {
			m_last2 = 0;
			m_last1 = 1;
		}
	}
}
