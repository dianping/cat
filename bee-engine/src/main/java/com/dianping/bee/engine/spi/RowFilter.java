package com.dianping.bee.engine.spi;

public interface RowFilter {
	public boolean filter(RowContext ctx);
}
