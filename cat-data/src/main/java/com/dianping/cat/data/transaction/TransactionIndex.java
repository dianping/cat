package com.dianping.cat.data.transaction;

import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.AbstractIndexMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

public class TransactionIndex extends AbstractIndexMeta<TransactionColumn> implements IndexMeta {
	public static final TransactionIndex IDX_DOMAIN = new TransactionIndex(TransactionColumn.Domain, true,
	      TransactionColumn.StartTime, false);

	private TransactionIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index<?>> getIndexClass() {
		if (this == IDX_DOMAIN) {
			return TransactionIndexer.class;
		} else {
			throw new UnsupportedOperationException("No index defined for index: " + this);
		}
	}

	public static TransactionIndex[] values() {
		return new TransactionIndex[] { IDX_DOMAIN };
	}
}