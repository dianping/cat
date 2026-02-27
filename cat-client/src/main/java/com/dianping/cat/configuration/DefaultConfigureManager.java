package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.model.IEntity;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Host;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.configuration.model.transform.BaseVisitor;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

// Component
public class DefaultConfigureManager implements ConfigureManager, Initializable, LogEnabled {
	private ClientConfig m_config = new ClientConfig();

	private List<Refreshable> m_refreshables = new ArrayList<>();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public boolean getBooleanProperty(String name, boolean defaultValue) {
		String property = getProperty(name, null);

		if (property != null) {
			try {
				return Boolean.valueOf(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public String getDomain() {
		return m_config.getDomain().getName();
	}

	@Override
	public double getDoubleProperty(String name, double defaultValue) {
		String property = getProperty(name, null);

		if (property != null) {
			try {
				return Double.parseDouble(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public Host getHost() {
		return m_config.getHost();
	}

	@Override
	public int getIntProperty(String name, int defaultValue) {
		String property = getProperty(name, null);

		if (property != null) {
			try {
				return Integer.parseInt(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public long getLongProperty(String name, long defaultValue) {
		String property = getProperty(name, null);

		if (property != null) {
			try {
				return Long.parseLong(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		Property property = m_config.findProperty(name);

		if (property != null) {
			return property.getValue();
		}

		return defaultValue;
	}

	@Override
	public List<Server> getServers() {
		return m_config.getServers();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void initialize(ComponentContext ctx) {
		List<ConfigureSource> sources = ctx.lookupList(ConfigureSource.class);

		Collections.sort(sources, new Comparator<ConfigureSource>() {
			@Override
			public int compare(ConfigureSource o1, ConfigureSource o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});

		for (ConfigureSource source : sources) {
			try {
				IEntity config = source.getConfig();

				if (config != null) {
					config.accept(new ConfigureApplier());
				}
			} catch (Exception e) {
				m_logger.warn(e, "Error when getting configure from %s", source.getClass());
			}
		}

		m_config.accept(new ConfigureValidator());

		for (ConfigureSource source : sources) {
			if (source instanceof Refreshable) {
				m_refreshables.add((Refreshable) source);
			}
		}

		if (!Cat.getBootstrap().isTestMode()) {
			if (!m_refreshables.isEmpty()) {
				Threads.forGroup("Cat").start(new ConfigureRefresher());
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return m_config.isEnabled();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		Host host = m_config.getHost();

		sb.append("domain: ").append(m_config.getDomain().getName()).append(", ");
		sb.append("host: ").append(host.getIp()).append('(').append(host.getName()).append("), ");
		sb.append("servers: ");

		for (Server server : m_config.getServers()) {
			sb.append(server.getIp()).append(':').append(server.getPort());
			sb.append('/').append(server.getHttpPort()).append(", ");
		}

		return sb.substring(0, sb.length() - 2);
	}

	private class ConfigureApplier extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getHost() != null) {
				visitHost(config.getHost());
			}

			if (config.getDomain() != null) {
				visitDomain(config.getDomain());
			}

			if (m_config.getServers().isEmpty()) {
				for (Server server : config.getServers()) {
					visitServer(server);
				}
			}

			for (Property property : config.getProperties().values()) {
				visitProperty(property);
			}
		}

		@Override
		public void visitDomain(Domain domain) {
			Domain d = m_config.getDomain();

			if (d == null) {
				d = new Domain().setName(domain.getName()).setTenantToken(domain.getTenantToken());

				m_config.setDomain(d);
			}
		}

		@Override
		public void visitHost(Host host) {
			Host h = m_config.getHost();

			if (h == null) {
				h = new Host().setIp(host.getIp());

				m_config.setHost(host);
			}
		}

		@Override
		public void visitProperty(Property property) {
			Property p = m_config.findOrCreateProperty(property.getName());

			p.setValue(property.getValue());
		}

		@Override
		public void visitServer(Server server) {
			if (server.isEnabled()) {
				Server s = m_config.findOrCreateServer(server.getIp());

				s.setIp(server.getIp());
				s.setPort(server.getPort());
				s.setHttpPort(server.getHttpPort());
			}
		}
	}

	private class ConfigureRefresher implements Task {
		private AtomicBoolean m_enabled = new AtomicBoolean(true);

		private CountDownLatch m_latch = new CountDownLatch(1);

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		private void refresh() {
			for (Refreshable refreshable : m_refreshables) {
				try {
					ClientConfig config = refreshable.refresh(m_config);

					config.accept(new ConfigureApplier());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public void run() {
			long interval = 60 * 1000L;

			try {
				Threads.sleep(m_enabled, interval);

				while (m_enabled.get()) {
					long now = System.currentTimeMillis();

					refresh();

					Threads.sleep(m_enabled, now + interval - System.currentTimeMillis());
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

	// check if the ClientConfig is well prepared
	// DISABLE CAT if anything required is missing
	private static class ConfigureValidator extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomain() == null) {
				config.setDomain(new Domain().setName("Unknown"));
			}

			if (config.getHost() == null) {
				Host host = new Host();

				host.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
				host.setName(NetworkInterfaceManager.INSTANCE.getLocalHostName());
				config.setHost(host);
			}

			if (config.getServers().isEmpty()) {
				config.setEnabled(false);
			}
		}
	}
}
