package com.dianping.cat.message.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.KVConfig;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.helper.JsonBuilder;

public class ChannelManager implements Task {

	private ClientConfigManager m_configManager;

	private Bootstrap m_bootstrap;

	private Logger m_logger;

	private boolean m_active = true;

	private int m_retriedTimes = 0;

	private int m_count = -10;

	private volatile double m_sample = 1d;

	private MessageQueue m_queue;

	private ChannelHolder m_activeChannelHolder;

	private MessageIdFactory m_idfactory;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	public ChannelManager(Logger logger, List<InetSocketAddress> serverAddresses, MessageQueue queue,
	      ClientConfigManager configManager, MessageIdFactory idFactory) {
		m_logger = logger;
		m_queue = queue;
		m_configManager = configManager;
		m_idfactory = idFactory;

		EventLoopGroup group = new NioEventLoopGroup(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});

		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
			}
		});
		m_bootstrap = bootstrap;

		String serverConfig = loadServerConfig();

		if (StringUtils.isNotEmpty(serverConfig)) {
			List<InetSocketAddress> configedAddresses = parseSocketAddress(serverConfig);
			ChannelHolder holder = initChannel(configedAddresses, serverConfig);

			if (holder != null) {
				m_activeChannelHolder = holder;
			} else {
				m_activeChannelHolder = new ChannelHolder();
				m_activeChannelHolder.setServerAddresses(configedAddresses);
			}
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

	public ChannelFuture channel() {
		if (m_activeChannelHolder != null) {
			return m_activeChannelHolder.getActiveFuture();
		} else {
			return null;
		}
	}

	private void checkServerChanged() {
		if (shouldCheckServerConfig(++m_count)) {
			Pair<Boolean, String> pair = routerConfigChanged();

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
	}

	private void closeChannel(ChannelFuture channel) {
		try {
			if (channel != null) {
				m_logger.info("close channel " + channel.channel().remoteAddress());
				channel.channel().close();
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
				m_logger.error("Error when try connecting to " + address);
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

	private void doubleCheckActiveServer(ChannelFuture activeFuture) {
		try {
			if (isChannelStalled(activeFuture) || isChannelDisabled(activeFuture)) {
				closeChannelHolder(m_activeChannelHolder);
			}
		} catch (Throwable e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getName() {
		return "TcpSocketSender-ChannelManager";
	}

	public double getSample() {
		return m_sample;
	}

	private ChannelHolder initChannel(List<InetSocketAddress> addresses, String serverConfig) {
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

		try {
			StringBuilder sb = new StringBuilder();

			for (InetSocketAddress address : addresses) {
				sb.append(address.toString()).append(";");
			}
			m_logger.info("Error when init CAT server " + sb.toString());
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	private boolean isChannelDisabled(ChannelFuture activeFuture) {
		return activeFuture != null && !activeFuture.channel().isOpen();
	}

	private boolean isChannelStalled(ChannelFuture activeFuture) {
		m_retriedTimes++;

		int size = m_queue.size();
		boolean stalled = activeFuture != null && size >= TcpSocketSender.getQueueSize() - 10;

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
			InputStream inputstream = Urls.forIO().readTimeout(2000).connectTimeout(1000).openStream(url);
			String content = Files.forIO().readFrom(inputstream, "utf-8");

			KVConfig routerConfig = (KVConfig) m_jsonBuilder.parse(content.trim(), KVConfig.class);
			String current = routerConfig.getValue("routers");
			m_sample = Double.valueOf(routerConfig.getValue("sample").trim());

			return current.trim();
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

	private void reconnectDefaultServer(ChannelFuture activeFuture, List<InetSocketAddress> serverAddresses) {
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
	}

	private Pair<Boolean, String> routerConfigChanged() {
		String current = loadServerConfig();

		if (!StringUtils.isEmpty(current) && !current.equals(m_activeChannelHolder.getActiveServerConfig())) {
			return new Pair<Boolean, String>(true, current);
		} else {
			return new Pair<Boolean, String>(false, current);
		}
	}

	@Override
	public void run() {
		while (m_active) {
			// make save message id index asyc
			m_idfactory.saveMark();
			checkServerChanged();

			ChannelFuture activeFuture = m_activeChannelHolder.getActiveFuture();
			List<InetSocketAddress> serverAddresses = m_activeChannelHolder.getServerAddresses();

			doubleCheckActiveServer(activeFuture);
			reconnectDefaultServer(activeFuture, serverAddresses);

			try {
				Thread.sleep(10 * 1000L); // check every 10 seconds
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private boolean shouldCheckServerConfig(int count) {
		int duration = 30;

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

	public class ClientMessageHandler extends SimpleChannelInboundHandler<Object> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
			m_logger.info("receiver msg from server:" + msg);
		}
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

			sb.append("active future :").append(m_activeFuture.channel().remoteAddress());
			sb.append(" index:").append(m_activeIndex);
			sb.append(" ip:").append(m_ip);
			sb.append(" server config:").append(m_activeServerConfig);
			return sb.toString();
		}
	}

}
