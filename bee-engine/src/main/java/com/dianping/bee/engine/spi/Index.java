package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface Index {
	public int getLength();

	public ColumnMeta getColumn(int index);

	public boolean isAscend(int index);
}
