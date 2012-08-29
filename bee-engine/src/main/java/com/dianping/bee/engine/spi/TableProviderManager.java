package com.dianping.bee.engine.spi;

public interface TableProviderManager {
	public TableProvider getTableProvider(String table);

	public TableProvider getTableProvider(String database, String table);
}
