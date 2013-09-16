package com.dianping.cat.configuration;

import java.text.MessageFormat;
import java.util.Date;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultValidator;

public class ClientConfigValidator extends DefaultValidator {
	private ClientConfig m_config;

	private String getLocalAddress() {
		return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	@Override
	public void visitConfig(ClientConfig config) {
		if (!"client".equals(config.getMode())) {
			throw new RuntimeException(String.format("Attribute(%s) of /config is required in config: %s", "mode", config));
		} else if (config.getServers().size() == 0) {
			config.setEnabled(false);
			log("WARN", "CAT client was disabled due to no CAT servers configured!");
		} else if (config.getEnabled() != null && !config.isEnabled()) {
			log("WARN", "CAT client was globally disabled!");
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
			log("WARN", "CAT client was disabled in domain(" + domain.getId() + ") explicitly!");
		}
	}

	private void log(String severity, String message) {
		MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

		System.out.println(format.format(new Object[] { new Date(), severity, "Cat", message }));
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
