package com.dianping.cat.message.io;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
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
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;
import org.unidal.tuple.Pair;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.helper.Splitters;

public class ChannelManager implements Task {
	private List<InetSocketAddress> m_serverAddresses;

	private ClientConfigManager m_configManager;

	private ClientBootstrap m_bootstrap;

	private ChannelFuture m_activeFuture;

	private Logger m_logger;

	private ChannelFuture m_lastFuture;

	private boolean m_active = true;

	private int m_activeIndex = -1;

	private int m_retriedTimes = 0;

	private int m_count = 1;

	private volatile int m_error = -1;

	public static final int SIZE = 10000;

	private AtomicInteger m_reconnects = new AtomicInteger(99);

	private MessageQueue m_queue;

	private String m_activeServerConfig;

	public ChannelManager(Logger logger, List<InetSocketAddress> serverAddresses, MessageQueue queue,
	      ClientConfigManager configManager) {
		m_logger = logger;
		m_queue = queue;
		m_configManager = configManager;

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

		String serverConfig = loadServerConfig();

		if (serverConfig != null) {
			List<InetSocketAddress> newAddress = parseSocketAddress(serverConfig);

			initChannel(newAddress, serverConfig);
		} else {
			initChannel(serverAddresses, null);
		}
	}

	private void closeAllChannel() {
		try {
			if (m_activeFuture != null) {
				m_activeFuture.getChannel().close();
			}
			if (m_lastFuture != null) {
				m_lastFuture.getChannel().close();
			}
			m_activeIndex = -1;
		} catch (Exception e) {
			// ignore
		}
	}

	private ChannelFuture createChannel(InetSocketAddress address) {
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

	private void initChannel(List<InetSocketAddress> addresses, String serverConfig) {
		try {
			StringBuilder sb = new StringBuilder();

			for (InetSocketAddress address : addresses) {
				sb.append(address.getAddress().getHostAddress()).append(":").append(address.getPort()).append(',');
			}
			m_logger.info("init CAT server:" + sb.toString());

			m_serverAddresses = addresses;
			int len = addresses.size();

			for (int i = 0; i < len; i++) {
				ChannelFuture future = createChannel(addresses.get(i));

				if (future != null) {
					m_activeFuture = future;
					m_activeIndex = i;
					m_activeServerConfig = serverConfig;
					break;
				}
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			// ignore
		}
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

	private String loadServerConfig() {
		try {
			String url = m_configManager.getServerConfigUrl();
			InputStream currentServer = Urls.forIO().readTimeout(2000).connectTimeout(1000).openStream(url);
			String content = Files.forIO().readFrom(currentServer, "utf-8");

			return content.trim();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<InetSocketAddress> parseSocketAddress(String content) {
		try {
			List<String> strs = Splitters.by(";").noEmptyItem().split(content);
			List<InetSocketAddress> address = new ArrayList<InetSocketAddress>();

			for (String str : strs) {
				List<String> items = Splitters.by(":").noEmptyItem().split(str);

				address.add(new InetSocketAddress(items.get(0), Integer.parseInt(items.get(1))));
			}
			return address;
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		return new ArrayList<InetSocketAddress>();
	}

	@Override
	public void run() {
		while (m_active) {
			m_count++;

			if (shouldCheckServerConfig(m_count)) {
				Pair<Boolean, String> pair = serverConfigChanged();

				if (pair.getKey()) {
					closeAllChannel();

					String servers = pair.getValue();
					List<InetSocketAddress> serverAddresses = parseSocketAddress(servers);

					initChannel(serverAddresses, servers);
				}
			}

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
					ChannelFuture future = createChannel(m_serverAddresses.get(i));

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

	private Pair<Boolean, String> serverConfigChanged() {
		String current = loadServerConfig();

		if (current != null && !current.equals(m_activeServerConfig)) {
			return new Pair<Boolean, String>(true, current);
		} else {
			return new Pair<Boolean, String>(false, current);
		}
	}

	private boolean shouldCheckServerConfig(int count) {
		int duration = 60 * 5;

		if (count % (duration) == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void shutdown() {
		m_active = false;
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