package com.dianping.bee.engine.spi;

import java.util.List;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface Statement {
	public List<ColumnMeta> getSelectColumns();

	public Index getIndex();

	public RowFilter getRowFilter();

	public String getTableName();

	public void setRowFilter(RowFilter rowFilter);

	public void setSelectColumns(List<ColumnMeta> selectColumns);
}
