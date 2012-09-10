package com.dianping.bee.engine.spi;


public interface TableProvider {
	public ColumnMeta[] getColumns();

	public IndexMeta getDefaultIndex();

	public IndexMeta[] getIndexes();

	public String getName();
}
