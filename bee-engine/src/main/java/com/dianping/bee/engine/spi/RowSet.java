package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface RowSet {
	public ColumnMeta getColumn(int colIndex);

	public int getColumns();

	public Row getRow(int rowIndex);

	public int getRows();
}
