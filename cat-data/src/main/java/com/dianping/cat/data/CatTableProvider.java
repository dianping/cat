package com.dianping.cat.data;

import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Index;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.cat.data.event.EventColumn;
import com.dianping.cat.data.event.EventIndex;
import com.dianping.cat.data.transaction.TransactionColumn;
import com.dianping.cat.data.transaction.TransactionIndex;

public enum CatTableProvider implements TableProvider {
	Transaction("transaction", TransactionColumn.values(), TransactionIndex.values()),

	Event("event", EventColumn.values(), EventIndex.values()),

	Heartbeat("heartbeat"),

	Problem("problem");

	private String m_name;

	private ColumnMeta[] m_columns;

	private Index[] m_indexes;

	private CatTableProvider(String name) {
		m_name = name;
	}

	private CatTableProvider(String name, ColumnMeta[] columns, Index[] indexes) {
		m_name = name;
		m_columns = columns;
		m_indexes = indexes;
	}

	@Override
	public ColumnMeta[] getColumns() {
		return m_columns;
	}

	@Override
	public Index[] getIndexes() {
		return m_indexes;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public RowSet queryByIndex(Index index, ColumnMeta[] selectColumns) {
		return null;
	}
}