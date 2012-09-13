package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dianping.bee.engine.spi.Session;

public class DefaultSession implements Session {
	private String m_database;

	private Map<String, Object> m_metadata;

	@Override
	public String getDatabase() {
		return m_database;
	}

	@Override
	public void setDatabase(String database) {
		m_database = database;
	}

	@Override
	public void setMetadata(Map<String, Object> metadata) {
		this.m_metadata = metadata;
	}

	@Override
	public Map<String, Object> getMetadata() {
		if (this.m_metadata == null) {
			this.m_metadata = getDefaultMetadat();
		}
		return this.m_metadata;
	}

	/**
	 * @return
	 */
	private Map<String, Object> getDefaultMetadat() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("language", "");

		// timeout
		map.put("net_write_timeout", "60");
		map.put("interactive_timeout", "28800");
		map.put("wait_timeout", "28800");

		// character_set
		map.put("character_set_client", System.getProperty("sun.jnu.encoding"));
		map.put("character_set_connection", System.getProperty("sun.jnu.encoding"));
		map.put("character_set", new ArrayList<String>());
		map.put("character_set_server", System.getProperty("sun.jnu.encoding"));
		map.put("character_set_results", System.getProperty("sun.jnu.encoding"));

		// transaction
		map.put("tx_isolation", "REPEATABLE-READ");
		map.put("transaction_isolation", "REPEATABLE-READ");

		// zone
		map.put("timezone", "");
		map.put("time_zone", "SYSTEM");
		map.put("system_time_zone", "");

		// length
		map.put("lower_case_table_names", "1");
		map.put("max_allowed_packet", "1048576");
		map.put("net_buffer_length", "8192");
		map.put("sql_mode", "");
		map.put("query_cache_type", "ON");
		map.put("query_cache_size", "0");
		map.put("init_connect", "");

		map.put("auto_increment_increment", "1");
		return map;
	}
}
