package com.dianping.bee.engine.spi;

import java.util.Map;

public interface Session {
	public String getDatabase();

	public void setDatabase(String database);

	public void setMetadata(Map<String, Object> metadata);

	public Map<String, Object> getMetadata();
}
