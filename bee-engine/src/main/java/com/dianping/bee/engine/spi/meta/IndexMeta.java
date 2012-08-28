package com.dianping.bee.engine.spi.meta;

public interface IndexMeta {
	public int getLength();

	public ColumnMeta getColumn(int index);

	public boolean isAscend(int index);
}
