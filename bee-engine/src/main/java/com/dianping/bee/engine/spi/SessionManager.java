package com.dianping.bee.engine.spi;

public interface SessionManager {
	public Session getSession();

	public void removeSession();
}
