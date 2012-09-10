package com.dianping.bee.engine;


public interface Row {
	public Cell getCell(int colIndex);

	public int getColumnSize();
}
