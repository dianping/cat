package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.MultiTableStatement;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.RowSet;
import com.dianping.bee.engine.spi.meta.ColumnMeta;

public class DefaultMultiTableStatement implements MultiTableStatement {
	private List<String> m_tableNames;

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
	public List<String> getTableNames() {
		return m_tableNames;
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
	public void setTableNames(List<String> tableNames) {
		m_tableNames = tableNames;
	}

	@Override
   public RowSet query() {
	   return null;
   }
}
