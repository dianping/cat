package com.dianping.bee.engine.spi;

public interface TableProviderManager {
	public String getDatabaseName();

	public TableProvider getTableProvider(String table);
}
