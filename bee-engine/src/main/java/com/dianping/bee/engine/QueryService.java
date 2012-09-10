package com.dianping.bee.engine;

import java.sql.SQLException;

import com.dianping.bee.engine.spi.meta.RowSet;

public interface QueryService {
	public RowSet query(String sql, Object... params) throws SQLException;

	public void use(String database);
}
