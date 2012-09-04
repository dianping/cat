package com.dianping.bee.engine.spi.session;

public interface SessionManager {
	public Session getSession();

	public void removeSession();
}
