package com.dianping.cat.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.Refreshable;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.support.Files;
import com.dianping.cat.support.Splitters;
import com.dianping.cat.support.Urls;

public class ServerConfigureSource implements ConfigureSource<ClientConfig>, Refreshable, LogEnabled {
	private static final String REFRESH_URL_PATTERN = "http://%s:%d/cat/s/router?op=json&domain=%s&ip=%s&token=%s";

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public ClientConfig getConfig() throws Exception {
		return new ClientConfig();
	}

	@Override
	public int getOrder() {
		return 100;
	}

	private void parseMap(Map<String, String> map, String content) {
		if (content.startsWith("{") && content.endsWith("}")) {
			String keyValuePairs = content.substring(1, content.length() - 1);

			Splitters.by(',', '=').trim().split(keyValuePairs, map);
		}
	}

	@Override
	public ClientConfig refresh(ClientConfig config) throws Exception {
		Map<String, String> properties = new HashMap<String, String>();
		String url = null;

		for (Server server : config.getServers()) {
			try {
				String ip = server.getIp();
				int port = server.getHttpPort();
				String localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				String domain = config.getDomain().getName();
				String token = config.getDomain().getTenantToken();

				url = String.format(REFRESH_URL_PATTERN, ip, port, domain, localIp, token);

				InputStream inputstream = Urls.forIO().connectTimeout(1000).readTimeout(1000).openStream(url);
				String content = Files.forIO().readFrom(inputstream, "utf-8");

				parseMap(properties, content.trim());
				break;
			} catch (IOException e) {
				// ignore it
				m_logger.warn(String.format("Error when requesting %s. Reason: %s. IGNORED", url, e));
			}
		}

		ClientConfig newConfig = new ClientConfig();

		for (Map.Entry<String, String> e : properties.entrySet()) {
			newConfig.addProperty(new Property(e.getKey()).setValue(e.getValue()));
		}

		return newConfig;
	}
}
