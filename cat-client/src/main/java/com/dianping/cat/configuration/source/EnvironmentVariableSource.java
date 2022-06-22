package com.dianping.cat.configuration.source;

import java.util.List;

import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Host;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.support.Splitters;

public class EnvironmentVariableSource implements ConfigureSource<ClientConfig> {
	@Override
	public ClientConfig getConfig() throws Exception {
		ClientConfig config = new ClientConfig();
		boolean dirty = false;

		// host section
		String hostIp = System.getenv("CAT_HOST_ID");

		if (hostIp != null && hostIp.length() > 0) {
			config.setHost(new Host().setIp(hostIp));
			dirty = true;
		}

		// server section
		String serverIps = System.getenv("CAT_SERVER_IPS");
		String serverPort = System.getenv("CAT_SERVER_PORT");

		if (serverIps != null && serverIps.length() > 0) {
			List<String> ips = Splitters.by(',').trim().noEmptyItem().split(serverIps);
			int port = toInt(serverPort, 8080);

			for (String ip : ips) {
				config.addServer(new Server().setIp(ip).setHttpPort(port));
				dirty = true;
			}
		}

		if (dirty) {
			return config;
		} else {
			return null;
		}
	}

	private int toInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	@Override
	public int getOrder() {
		return 210;
	}
}
