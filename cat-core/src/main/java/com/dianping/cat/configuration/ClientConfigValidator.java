package com.dianping.cat.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.configuration.model.transform.DefaultValidator;

public class ClientConfigValidator extends DefaultValidator {
	private String getLocalAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// ignore it
		}

		return null;
	}

	@Override
	public void visitConfig(Config config) {
		if (!"client".equals(config.getMode())) {
			throw new RuntimeException(String.format("Attribute(%s) of /config is required in config: %s", "mode", config));
		} else if (config.getServers().size() == 0) {
			throw new RuntimeException(
			      String.format("Element(%s) of /config is required in config: %s", "servers", config));
		}

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
