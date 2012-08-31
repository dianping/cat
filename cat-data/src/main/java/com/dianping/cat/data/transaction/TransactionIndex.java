package com.dianping.cat.data.transaction;

import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.AbstractIndexMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

public class TransactionIndex extends AbstractIndexMeta<TransactionColumn> implements IndexMeta {
	public static final TransactionIndex IDX_STARTTIME_DOMAIN = new TransactionIndex(TransactionColumn.StartTime, false,
	      TransactionColumn.Domain, true);

	private TransactionIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index<?>> getIndexClass() {
		return TransactionIndexer.class;
	}

	public static TransactionIndex[] values() {
		return new TransactionIndex[] { IDX_STARTTIME_DOMAIN };
	}
}