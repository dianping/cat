package com.dianping.cat.data.transaction;

import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.TableExecutor;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.DefaultRowSet;

public class TransactionExecutor implements TableExecutor<TransactionIndex, TransactionColumn> {
	@Override
	public RowSet execute(TransactionIndex index, TransactionColumn[] columns, RowFilter filter) {
		RowSet rowset = new DefaultRowSet(columns);

		if (index == TransactionIndex.IDX_STARTTIME_DOMAIN) {

		} else {
			
		}

		return rowset;
	}
}
