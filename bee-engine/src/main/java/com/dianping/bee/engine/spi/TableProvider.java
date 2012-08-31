package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface TableProvider {
	public ColumnMeta[] getColumns();

	public IndexMeta[] getIndexes();

	public String getName();

	public RowSet queryByIndex(IndexMeta m_index, ColumnMeta[] m_selectColumns) throws Exception;
}
