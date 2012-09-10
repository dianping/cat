package com.dianping.bee.engine;

import com.dianping.bee.engine.spi.ColumnMeta;

public interface Cell {
	public ColumnMeta getMeta();

	public Object getValue();
}
