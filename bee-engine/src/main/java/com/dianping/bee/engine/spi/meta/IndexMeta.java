package com.dianping.bee.engine.spi.meta;

import com.dianping.bee.engine.spi.index.Index;

public interface IndexMeta {
	public int getLength();

	public ColumnMeta getColumn(int index);

	public boolean isAscend(int index);

	public Class<? extends Index<?>> getIndexClass();
}
