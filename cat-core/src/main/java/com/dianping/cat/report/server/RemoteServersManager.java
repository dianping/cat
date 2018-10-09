package com.dianping.cat.report.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.service.ModelPeriod;

@Named
public class RemoteServersManager {

	private volatile Map<String, Set<String>> m_currentServers = new ConcurrentHashMap<String, Set<String>>();

	private volatile Map<String, Set<String>> m_lastServers = new ConcurrentHashMap<String, Set<String>>();

	public Set<String> queryServers(String domain, long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT) {
			return m_currentServers.get(domain);
		} else if (period == ModelPeriod.LAST) {
			return m_lastServers.get(domain);
		} else {
			return null;
		}
	}

	public void setCurrentServers(Map<String, Set<String>> currentServers) {
		m_currentServers = currentServers;
	}

	public void setLastServers(Map<String, Set<String>> lastServers) {
		m_lastServers = lastServers;
	}

}
