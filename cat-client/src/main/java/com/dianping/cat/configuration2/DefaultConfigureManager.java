package com.dianping.cat.configuration2;

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
import com.dianping.cat.configure.client2.IEntity;
import com.dianping.cat.configure.client2.entity.ClientConfig;
import com.dianping.cat.configure.client2.entity.Domain;
import com.dianping.cat.configure.client2.entity.Host;
import com.dianping.cat.configure.client2.entity.Property;
import com.dianping.cat.configure.client2.entity.Server;
import com.dianping.cat.configure.client2.transform.BaseVisitor;
import com.dianping.cat.util.Threads;
import com.dianping.cat.util.Threads.Task;

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
	public String getDomain() {
		return m_config.getDomain().getName();
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
			System.out.println(source);
			try {
				IEntity config = source.getConfig();

				if (config != null) {
					System.out.println("Before: " + m_config);
					config.accept(new ConfigureApplier());
					System.out.println("After: " + m_config);
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

		if (!m_refreshables.isEmpty()) {
			Threads.forGroup("Cat").start(new ConfigureRefresher());
		}

		System.out.println(m_config);
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

			for (Property property : config.getProperties()) {
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

	// check if the ClientConfig is well prepared
	// DISABLE CAT if anything required is missing
	private static class ConfigureValidator extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomain() == null) {
				config.setDomain(new Domain().setName("Unknown"));
			}

			if (config.getServers().isEmpty()) {
				config.setEnabled(false);
			}
		}
	}

	private class ConfigureRefresher implements Task {
		private AtomicBoolean m_enabled = new AtomicBoolean(true);

		private CountDownLatch m_latch = new CountDownLatch(1);

		@Override
		public String getName() {
			return getClass().getName();
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
}
