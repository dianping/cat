package com.dianping.bee.engine.spi.meta;

public interface RowSet {
	public ColumnMeta getColumn(int colIndex);

	public int getColumnSize();

	public Row getRow(int rowIndex);

	public int getRowSize();
}
