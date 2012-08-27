package com.dianping.cat.data;

import com.dianping.bee.engine.spi.DatabaseProvider;

public class CatDatabaseProvider implements DatabaseProvider {
	public static final String ID = "cat";
	
	@Override
	public String getName() {
		return ID;
	}

	@Override
	public CatTableProvider[] getTables() {
		return CatTableProvider.values();
	}
}
