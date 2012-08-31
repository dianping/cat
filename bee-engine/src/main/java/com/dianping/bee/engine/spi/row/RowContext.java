package com.dianping.bee.engine.spi.row;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface RowContext {

	public int getColumnSize();

	public <T extends ColumnMeta> T getColumn(int colIndex);

	public <T> T getValue(int colIndex);

	public void apply();

	public void setColumnValue(int colIndex, Object value);

	public void setRowListener(RowListener listener);

}
