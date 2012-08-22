package com.dianping.bee.engine.spi;

import java.util.List;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface Statement {
	public Index getIndex();

	public RowFilter getRowFilter();

	public List<ColumnMeta> getSelectColumns();

	public String getTableName();

	public void setIndex(Index index);

	public void setRowFilter(RowFilter rowFilter);

	public void setSelectColumns(List<ColumnMeta> selectColumns);

	public void setTableName(String tableName);
}
