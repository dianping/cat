package com.dianping.bee.engine.spi.session;

public class DefaultSession implements Session {
	private String m_database;

	@Override
	public String getDatabase() {
		return m_database;
	}

	@Override
	public void setDatabase(String database) {
		m_database = database;
	}
}
