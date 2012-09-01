package com.dianping.bee.engine.spi.internal;

import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.row.DefaultRowContext;
import com.dianping.bee.engine.spi.row.DefaultRowListener;
import com.dianping.bee.engine.spi.row.RowContext;
import com.dianping.bee.engine.spi.row.RowFilter;
import com.site.lookup.ContainerHolder;

public class SingleTableStatement extends ContainerHolder implements Statement {
	private TableProvider m_table;

	private RowFilter m_rowFilter;

	private IndexMeta m_index;

	private ColumnMeta[] m_selectColumns;

	private int m_parameterSize;

	@Override
	public int getParameterSize() {
		return m_parameterSize;
	}

	@Override
	public ColumnMeta[] getSelectColumns() {
		return m_selectColumns;
	}

	@Override
	public RowSet query() {
		Index<?> index = lookup(m_index.getIndexClass());
		RowContext ctx = new DefaultRowContext(m_selectColumns);
		DefaultRowListener listener = new DefaultRowListener(m_selectColumns);

		listener.setRowFilter(m_rowFilter);
		ctx.setRowListener(listener);

		try {
			index.queryById(ctx, null);

			return listener.getRowSet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIndex(IndexMeta index) {
		m_index = index;
	}

	@Override
	public void setParameterSize(int parameterSize) {
		m_parameterSize = parameterSize;
	}

	@Override
	public void setRowFilter(RowFilter rowFilter) {
		m_rowFilter = rowFilter;
	}

	@Override
	public void setSelectColumns(ColumnMeta[] selectColumns) {
		if (selectColumns != null && selectColumns.length > 0) {
			m_selectColumns = selectColumns;
		} else {
			m_selectColumns = m_table.getColumns();
		}
	}

	public void setTable(TableProvider table) {
		m_table = table;
	}

}
