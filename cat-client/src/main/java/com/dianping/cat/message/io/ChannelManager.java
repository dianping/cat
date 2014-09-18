package com.dianping.cat.message.io;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.helper.Splitters;

public class ChannelManager implements Task {

	private ClientConfigManager m_configManager;

	private ClientBootstrap m_bootstrap;

	private Logger m_logger;

	private boolean m_active = true;

	private int m_retriedTimes = 0;

	private int m_count = -10;

	private MessageQueue m_queue;

	private ChannelHolder m_activeChannelHolder;

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

		if (StringUtils.isNotEmpty(serverConfig)) {
			List<InetSocketAddress> configedAddresses = parseSocketAddress(serverConfig);
			ChannelHolder holder = initChannel(configedAddresses, serverConfig);

			m_activeChannelHolder = holder;
		} else {
			ChannelHolder holder = initChannel(serverAddresses, null);

			if (holder != null) {
				m_activeChannelHolder = holder;
			} else {
				m_activeChannelHolder = new ChannelHolder();
				m_activeChannelHolder.setServerAddresses(serverAddresses);
				m_logger.error("error when init cat module due to error config xml in /data/appdatas/cat/client.xml");
			}
		}
	}

	private void closeChannel(ChannelFuture channel) {
		try {
			if (channel != null) {
				m_logger.info("close channel " + channel.getChannel().getRemoteAddress());
				channel.getChannel().close();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	private void closeChannelHolder(ChannelHolder channelHolder) {
		try {
			ChannelFuture channel = channelHolder.getActiveFuture();

			closeChannel(channel);
			channelHolder.setActiveIndex(-1);
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
				m_logger.error("Error when try to connecting to " + address);
				closeChannel(future);
			} else {
				m_logger.info("Connected to CAT server at " + address);
				return future;
			}
		} catch (Throwable e) {
			m_logger.error("Error when connect server " + address.getAddress(), e);

			if (future != null) {
				closeChannel(future);
			}
		}
		return null;
	}

	public ChannelFuture getChannel() {
		return m_activeChannelHolder.getActiveFuture();
	}

	@Override
	public String getName() {
		return "TcpSocketSender-ChannelManager";
	}

	private ChannelHolder initChannel(List<InetSocketAddress> addresses, String serverConfig) {
		StringBuilder sb = new StringBuilder();

		for (InetSocketAddress address : addresses) {
			sb.append(address.getAddress().getHostAddress()).append(":").append(address.getPort()).append(',');
		}

		try {
			int len = addresses.size();

			for (int i = 0; i < len; i++) {
				InetSocketAddress address = addresses.get(i);
				String hostAddress = address.getAddress().getHostAddress();
				ChannelHolder holder = null;

				if (m_activeChannelHolder != null && hostAddress.equals(m_activeChannelHolder.getIp())) {
					holder = new ChannelHolder();
					holder.setActiveFuture(m_activeChannelHolder.getActiveFuture()).setConnectChanged(false);
				} else {
					ChannelFuture future = createChannel(address);

					if (future != null) {
						holder = new ChannelHolder();
						holder.setActiveFuture(future).setConnectChanged(true);
					}
				}
				if (holder != null) {
					holder.setActiveIndex(i).setIp(hostAddress);
					holder.setActiveServerConfig(serverConfig).setServerAddresses(addresses);

					m_logger.info("success when init CAT server, new active holder" + holder.toString());
					return holder;
				}
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}

		m_logger.info("Error when init CAT server " + sb.toString());
		return null;
	}

	private boolean isChannelDisabled(ChannelFuture activeFuture) {
		return activeFuture != null && !activeFuture.getChannel().isOpen();
	}

	private boolean isChannelStalled(ChannelFuture activeFuture) {
		m_retriedTimes++;

		int size = m_queue.size();
		boolean stalled = activeFuture != null && size >= TcpSocketSender.SIZE - 10;

		if (stalled) {
			if (m_retriedTimes >= 5) {
				m_retriedTimes = 0;
				return true;
			} else {
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
			// ignore
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
					String servers = pair.getValue();
					List<InetSocketAddress> serverAddresses = parseSocketAddress(servers);
					ChannelHolder newHolder = initChannel(serverAddresses, servers);

					if (newHolder != null) {
						if (newHolder.isConnectChanged()) {
							ChannelHolder last = m_activeChannelHolder;

							m_activeChannelHolder = newHolder;
							closeChannelHolder(last);
							m_logger.info("switch active channel to " + m_activeChannelHolder);
						} else {
							m_activeChannelHolder = newHolder;
						}
					}
				}
			}
			ChannelFuture activeFuture = m_activeChannelHolder.getActiveFuture();
			List<InetSocketAddress> serverAddresses = m_activeChannelHolder.getServerAddresses();

			try {
				if (isChannelStalled(activeFuture) || isChannelDisabled(activeFuture)) {
					closeChannelHolder(m_activeChannelHolder);
				}
			} catch (Throwable e) {
				m_logger.error(e.getMessage(), e);
			}
			try {
				int reconnectServers = m_activeChannelHolder.getActiveIndex();

				if (reconnectServers == -1) {
					reconnectServers = serverAddresses.size();
				}
				for (int i = 0; i < reconnectServers; i++) {
					ChannelFuture future = createChannel(serverAddresses.get(i));

					if (future != null) {
						ChannelFuture lastFuture = activeFuture;

						m_activeChannelHolder.setActiveFuture(future);
						m_activeChannelHolder.setActiveIndex(i);
						closeChannel(lastFuture);
						break;
					}
				}
			} catch (Throwable e) {
				m_logger.error(e.getMessage(), e);
			}

			try {
				Thread.sleep(10 * 1000L); // check every 10 seconds
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private Pair<Boolean, String> serverConfigChanged() {
		String current = loadServerConfig();

		if (!StringUtils.isEmpty(current) && !current.equals(m_activeChannelHolder.getActiveServerConfig())) {
			return new Pair<Boolean, String>(true, current);
		} else {
			return new Pair<Boolean, String>(false, current);
		}
	}

	private boolean shouldCheckServerConfig(int count) {
		int duration = 60 * 5;

		if (count % duration == 0 || m_activeChannelHolder.getActiveIndex() == -1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void shutdown() {
		m_active = false;
	}

	public static class ChannelHolder {
		private ChannelFuture m_activeFuture;

		private int m_activeIndex = -1;

		private String m_activeServerConfig;

		private List<InetSocketAddress> m_serverAddresses;

		private String m_ip;

		private boolean m_connectChanged;

		public ChannelFuture getActiveFuture() {
			return m_activeFuture;
		}

		public int getActiveIndex() {
			return m_activeIndex;
		}

		public String getActiveServerConfig() {
			return m_activeServerConfig;
		}

		public String getIp() {
			return m_ip;
		}

		public List<InetSocketAddress> getServerAddresses() {
			return m_serverAddresses;
		}

		public boolean isConnectChanged() {
			return m_connectChanged;
		}

		public ChannelHolder setActiveFuture(ChannelFuture activeFuture) {
			m_activeFuture = activeFuture;
			return this;
		}

		public ChannelHolder setActiveIndex(int activeIndex) {
			m_activeIndex = activeIndex;
			return this;
		}

		public ChannelHolder setActiveServerConfig(String activeServerConfig) {
			m_activeServerConfig = activeServerConfig;
			return this;
		}

		public ChannelHolder setConnectChanged(boolean connectChanged) {
			m_connectChanged = connectChanged;
			return this;
		}

		public ChannelHolder setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public ChannelHolder setServerAddresses(List<InetSocketAddress> serverAddresses) {
			m_serverAddresses = serverAddresses;
			return this;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("active future :").append(m_activeFuture.getChannel().getRemoteAddress());
			sb.append(" index:").append(m_activeIndex);
			sb.append(" ip:").append(m_ip);
			sb.append(" server config:").append(m_activeServerConfig);
			return sb.toString();
		}

	}

	private class ExceptionHandler extends SimpleChannelHandler {

		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			m_logger.warn("Channel disconnected by remote address: " + e.getChannel().getRemoteAddress());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			m_logger.warn("Channel disconnected due to " + e.getCause());
		}
	}

}