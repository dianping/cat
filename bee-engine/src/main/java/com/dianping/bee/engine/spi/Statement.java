package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.RowSet;

public interface Statement {

	public ColumnMeta getColumnMeta(int colIndex);

	public int getColumnSize();

	public RowSet query();
	
	public IndexMeta getIndexMeta();
}
