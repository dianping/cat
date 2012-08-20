package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public interface Cell {
	public ColumnMeta getMeta();

	public Object getValue();
}
