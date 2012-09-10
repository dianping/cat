package com.dianping.bee.engine.spi;


public interface Index {
	public void query(RowContext ctx) throws Exception;
}
