package com.dianping.bee.engine;

import com.dianping.bee.engine.spi.ColumnMeta;

public interface RowSet {
	public ColumnMeta getColumn(int colIndex);

	public int getColumnSize();

	public Row getRow(int rowIndex);

	public int getRowSize();

	public void addRow(Row row);
}
