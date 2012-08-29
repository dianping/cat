package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface Statement {
	public IndexMeta getIndex();

	public RowFilter getRowFilter();

	public ColumnMeta[] getSelectColumns();

	public void setIndex(IndexMeta index);

	public void setRowFilter(RowFilter rowFilter);

	public void setSelectColumns(ColumnMeta[] selectColumns);

	public RowSet query();
}
