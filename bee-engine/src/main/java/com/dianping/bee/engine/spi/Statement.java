package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface Statement {

	public ColumnMeta getColumnMeta(int colIndex);

	public int getColumnSize();

	public RowSet query();
}
