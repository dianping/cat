package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

public interface TableProvider {
	public ColumnMeta[] getColumns();

	public IndexMeta getDefaultIndex();

	public IndexMeta[] getIndexes();

	public String getName();
}
