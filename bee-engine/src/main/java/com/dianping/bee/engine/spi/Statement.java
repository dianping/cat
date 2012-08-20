package com.dianping.bee.engine.spi;

import java.util.List;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface Statement {
	public List<ColumnMeta> getSelectedColumns();

	public Index getIndex();

	public RowFilter getRowFilter();
}
