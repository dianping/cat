package com.dianping.bee.engine.spi.index;

import com.dianping.bee.engine.spi.row.RowContext;

public interface Index {
	public void query(RowContext ctx) throws Exception;
}
