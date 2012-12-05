package com.dianping.bee.engine.spi;

public interface DatabaseProvider {
	public String getName();

	public TableProvider[] getTables();
}