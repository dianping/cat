package com.dianping.bee.engine.spi.meta;


public interface Row {
	public Cell getCell(int colIndex);
	
	public int getCells();
}
