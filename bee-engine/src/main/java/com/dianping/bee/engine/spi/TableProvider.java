package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface TableProvider {
	public String getName();

	public ColumnMeta[] getColumns();

	public Index[] getIndexes();

	public RowSet query(Statement stmt);
}
