package com.dianping.cat.data;

import org.codehaus.plexus.PlexusContainer;

import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.row.DefaultRowListener;
import com.dianping.bee.engine.spi.row.RowContext;
import com.dianping.cat.data.event.EventColumn;
import com.dianping.cat.data.event.EventIndex;
import com.dianping.cat.data.transaction.TransactionColumn;
import com.dianping.cat.data.transaction.TransactionIndex;
import com.site.lookup.ContainerLoader;

public enum CatTableProvider implements TableProvider {
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

	@Override
	public RowSet queryByIndex(IndexMeta meta, ColumnMeta[] selectColumns) throws Exception {
		PlexusContainer container = ContainerLoader.getDefaultContainer();
		Index<?> index = container.lookup(meta.getIndexClass());
		RowContext ctx = container.lookup(RowContext.class);
		DefaultRowListener listener = new DefaultRowListener(selectColumns);

		ctx.setRowListener(listener);
		index.queryById(ctx, null);

		return listener.getRowSet();
	}
}