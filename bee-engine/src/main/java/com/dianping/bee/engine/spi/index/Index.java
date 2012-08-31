package com.dianping.bee.engine.spi.index;

import com.dianping.bee.engine.spi.row.RowContext;

public interface Index<T> {
	public void queryById(RowContext ctx, T id) throws Exception;

	public void queryByIds(RowContext ctx, T[] ids) throws Exception;

	public void queryByRange(RowContext ctx, T start, T end, RangeType rangeType) throws Exception;
}
