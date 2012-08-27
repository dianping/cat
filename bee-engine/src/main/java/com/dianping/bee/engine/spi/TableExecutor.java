package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public interface TableExecutor<S, T extends ColumnMeta> {
	public RowSet execute(S index, T[] columns, RowFilter filter);
}
