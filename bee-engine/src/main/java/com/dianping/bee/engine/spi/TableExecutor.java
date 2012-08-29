package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface TableExecutor {
	public void execute(RowSet rowset, IndexMeta index, Object[] values);
}
