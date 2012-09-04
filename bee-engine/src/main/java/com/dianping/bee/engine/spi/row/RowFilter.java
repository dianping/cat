package com.dianping.bee.engine.spi.row;

public interface RowFilter {
	public boolean filter(RowContext ctx);
}
