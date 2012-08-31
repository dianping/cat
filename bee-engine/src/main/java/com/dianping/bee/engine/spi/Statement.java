package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.row.RowFilter;

public interface Statement {
	public IndexMeta getIndex();

	public int getParameterSize();

	public RowFilter getRowFilter();

	public ColumnMeta[] getSelectColumns();

	public RowSet query();

	public void setIndex(IndexMeta index);

	public void setParameterSize(int m_parameterSize);

	public void setRowFilter(RowFilter rowFilter);

	public void setSelectColumns(ColumnMeta[] selectColumns);
}
