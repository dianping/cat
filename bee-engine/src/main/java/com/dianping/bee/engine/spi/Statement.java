package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Index;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface Statement {
	public Index getIndex();

	public RowFilter getRowFilter();

	public ColumnMeta[] getSelectColumns();

	public void setIndex(Index index);

	public void setRowFilter(RowFilter rowFilter);

	public void setSelectColumns(ColumnMeta[] selectColumns);

	public RowSet query();
}
