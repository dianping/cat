package com.dianping.cat.agent.monitor;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.internal.DefaultMessageManager;

public class CatServers {

	private String SYSTEM_URL = "http://%1$s/cat/r/monitor?op=batch";

	public String buildSystemUrl(String server) {
		return String.format(SYSTEM_URL, server);
	}

	public List<String> getServers() {
		DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();
		List<Server> servers = manager.getConfigManager().getServers();
		List<String> results = new ArrayList<String>();

		for (Server server : servers) {
			results.add(server.getIp() + ":" + server.getHttpPort());
		}
		return results;
	}
}
