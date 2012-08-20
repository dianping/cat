package com.dianping.bee.engine.spi;

import java.util.List;

public interface RowFilter {
	public boolean filter(List<Object> values);
}
