package com.dianping.bee.engine.spi.session;

public class DefaultSession implements Session {
	private String m_database;

	@Override
	public String getDatabase() {
		System.out.println(Thread.currentThread()+":"+m_database);
		return m_database;
	}

	@Override
	public void setDatabase(String database) {
		m_database = database;
		System.out.println(Thread.currentThread()+":"+m_database);
	}
}
