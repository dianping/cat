package com.dianping.cat.agent.monitor;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Server;

public class CatServers {

	@Inject
	private ClientConfigManager m_clientManager;

	private String SYSTEM_URL = "http://%1$s/cat/r/monitor?op=batch";

	public String buildSystemUrl(String server) {
		return String.format(SYSTEM_URL, server);
	}

	public List<String> getServers() {
		List<Server> servers = m_clientManager.getServers();
		List<String> results = new ArrayList<String>();

		for (Server server : servers) {
			results.add(server.getIp() + ":" + server.getHttpPort());
		}
		return results;
	}
}
