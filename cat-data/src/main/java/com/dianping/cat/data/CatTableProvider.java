package com.dianping.cat.data;

import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.cat.data.event.EventColumn;
import com.dianping.cat.data.event.EventIndex;
import com.dianping.cat.data.node.NodeColumn;
import com.dianping.cat.data.node.NodeIndex;
import com.dianping.cat.data.transaction.TransactionColumn;
import com.dianping.cat.data.transaction.TransactionIndex;

public enum CatTableProvider implements TableProvider {
	Node("node", NodeColumn.values(), NodeIndex.values()),
	
	Transaction("transaction", TransactionColumn.values(), TransactionIndex.values()),

	Event("event", EventColumn.values(), EventIndex.values()),

	Heartbeat("heartbeat"),

	Problem("problem");

	private String m_name;

	private ColumnMeta[] m_columns;

	private IndexMeta m_defaultIndex;

	private IndexMeta[] m_indexes;

	private CatTableProvider(String name) {
		m_name = name;
	}

	private CatTableProvider(String name, ColumnMeta[] columns, IndexMeta[] indexes) {
		m_name = name;
		m_columns = columns;
		m_defaultIndex = indexes.length > 0 ? indexes[0] : null;
		m_indexes = indexes;
	}

	@Override
	public ColumnMeta[] getColumns() {
		return m_columns;
	}

	@Override
	public IndexMeta getDefaultIndex() {
		if (m_defaultIndex == null) {
			throw new RuntimeException("No default index defined yet!");
		} else {
			return m_defaultIndex;
		}
	}

	@Override
	public IndexMeta[] getIndexes() {
		return m_indexes;
	}

	@Override
	public String getName() {
		return m_name;
	}
}