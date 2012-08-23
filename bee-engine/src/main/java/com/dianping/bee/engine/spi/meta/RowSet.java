package com.dianping.bee.engine.spi.meta;

import com.dianping.bee.engine.spi.RowFilter;

public interface RowSet {

	/**
	 * @param rowFilter
	 */
	public void filter(RowFilter rowFilter);

	public ColumnMeta getColumn(int colIndex);

	public int getColumns();

	public Row getRow(int rowIndex);

	public int getRows();
}
