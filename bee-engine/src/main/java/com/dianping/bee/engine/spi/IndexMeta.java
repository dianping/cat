package com.dianping.bee.engine.spi;

public interface IndexMeta {
	public int getLength();

	public ColumnMeta getColumn(int index);

	public boolean isAscend(int index);

	public Class<? extends Index> getIndexClass();
}
