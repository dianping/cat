package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.RowSet;
import com.dianping.bee.engine.spi.SingleTableStatement;
import com.dianping.bee.engine.spi.meta.ColumnMeta;

public class DefaultSingleTableStatement implements SingleTableStatement {
	private String m_tableName;

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
	public String getTableName() {
		return m_tableName;
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
	public void setTableName(String tableName) {
		m_tableName = tableName;
	}

	@Override
	public RowSet query() {
		return null;
	}
}
