package com.dianping.bee.engine.spi;

import java.util.List;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.TableMeta;

public interface TableProvider {
	public TableMeta getMeta();

	public List<ColumnMeta> getColumns();

	public List<Index> getIndexes();

	public RowSet query(List<ColumnMeta> selectedColumns, Index index, RowFilter filter);
}
