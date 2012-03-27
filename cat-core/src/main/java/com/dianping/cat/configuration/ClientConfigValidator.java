package com.dianping.cat.configuration;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.configuration.model.transform.DefaultValidator;

public class ClientConfigValidator extends DefaultValidator {
	private Config m_config;

	private String getLocalAddress() {
		return LocalIP.getAddress();
	}

	@Override
	public void visitConfig(Config config) {
		if (!"client".equals(config.getMode())) {
			throw new RuntimeException(String.format("Attribute(%s) of /config is required in config: %s", "mode", config));
		} else if (config.getServers().size() == 0) {
			config.setEnabled(false);
			System.out.println("[WARN] CAT client was disabled due to no CAT servers configured!");
		} else if (config.getEnabled() != null && !config.isEnabled()) {
			System.out.println("[WARN] CAT client was globally disabled!");
		} else if (config.getEnabled() == null) {
			config.setEnabled(true);
		}

		m_config = config;
		super.visitConfig(config);
	}

	@Override
	public void visitDomain(Domain domain) {
		super.visitDomain(domain);

		// set default values
		if (domain.getEnabled() == null) {
			domain.setEnabled(true);
		}

		if (domain.getIp() == null) {
			domain.setIp(getLocalAddress());
		}

		if (!domain.isEnabled() && m_config.isEnabled()) {
			m_config.setEnabled(false);
			System.out.println("[WARN] CAT client was disabled in domain(" + domain.getId() + ") explicitly!");
		}
	}

	@Override
	public void visitServer(Server server) {
		super.visitServer(server);

		// set default values
		if (server.getPort() == null) {
			server.setPort(2280);
		}

		if (server.getEnabled() == null) {
			server.setEnabled(true);
		}
	}
}
