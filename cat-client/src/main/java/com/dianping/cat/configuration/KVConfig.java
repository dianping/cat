package com.dianping.cat.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KVConfig {

	private Map<String, String> m_kvs = new HashMap<String, String>();

	public Set<String> getKeys() {
		return m_kvs.keySet();
	}

	public Map<String, String> getKvs() {
		return m_kvs;
	}

	public String getValue(String key) {
		return m_kvs.get(key);
	}

	public void setKvs(Map<String, String> kvs) {
		m_kvs = kvs;
	}
}
