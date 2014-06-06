package com.dianping.cat.status;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

public class DataMonitor {

	private Map<String, Double> m_datas = new ConcurrentHashMap<String, Double>();

	private static DataMonitor m_monitor = new DataMonitor();

	public static DataMonitor getInstance() {
		return m_monitor;
	}

	private DataMonitor() {
	}

	public void put(String group, String key, double value) {
		m_datas.put(group + ":" + key, value);
	}

	public Map<String, Double> getDatas() {
		return m_datas;
	}

}
