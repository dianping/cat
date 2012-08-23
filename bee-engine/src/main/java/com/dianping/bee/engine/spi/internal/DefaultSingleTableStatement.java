package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.SingleTableStatement;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Index;
import com.dianping.bee.engine.spi.meta.RowSet;

public class DefaultSingleTableStatement implements SingleTableStatement {
	private TableProvider m_table;

	private RowFilter m_rowFilter;

	private Index m_index;

	private List<ColumnMeta> m_selectColumns;

	@Override
	public Index getIndex() {
		return m_index;
	}

	@Override
	public RowFilter getRowFilter() {
		return m_rowFilter;
	}

	@Override
	public List<ColumnMeta> getSelectColumns() {
		return m_selectColumns;
	}

	@Override
	public TableProvider getTable() {
		return m_table;
	}

	@Override
	public void setIndex(Index index) {
		m_index = index;
	}

	@Override
	public void setRowFilter(RowFilter rowFilter) {
		m_rowFilter = rowFilter;
	}

	@Override
	public void setSelectColumns(List<ColumnMeta> selectColumns) {
		m_selectColumns = selectColumns;
	}

	@Override
	public void setTable(TableProvider table) {
		m_table = table;
	}

	@Override
	public RowSet query() {
		// Query By Index
		RowSet providerRowSet = m_table.queryByIndex(m_index, m_selectColumns);
		// Filter
		providerRowSet.filter(m_rowFilter);
		// Build select columns
		RowSet returnRowSet = buildReturnRowSet(providerRowSet);
		return returnRowSet;
	}

	/**
	 * @param providerRowSet
	 * @return
	 */
	private RowSet buildReturnRowSet(RowSet c) {
		return c;
	}
}
