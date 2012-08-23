package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Index;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface TableProvider {
	public ColumnMeta[] getColumns();

	public Index[] getIndexes();

	public String getName();

	/**
	 * @param m_index
	 * @param m_selectColumns
	 */
	public RowSet queryByIndex(Index m_index, ColumnMeta[] m_selectColumns);
}
