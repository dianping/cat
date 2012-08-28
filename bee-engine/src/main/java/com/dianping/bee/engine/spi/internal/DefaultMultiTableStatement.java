package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.MultiTableStatement;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public class DefaultMultiTableStatement implements MultiTableStatement {
	private List<TableProvider> m_tables;

	private RowFilter m_rowFilter;

	private IndexMeta m_index;

	private ColumnMeta[] m_selectColumns;

	@Override
	public IndexMeta getIndex() {
		return m_index;
	}

	@Override
	public RowFilter getRowFilter() {
		return m_rowFilter;
	}

	@Override
	public ColumnMeta[] getSelectColumns() {
		return m_selectColumns;
	}

	@Override
	public List<TableProvider> getTables() {
		return m_tables;
	}

	@Override
	public void setIndex(IndexMeta index) {
		m_index = index;
	}

	@Override
	public void setRowFilter(RowFilter rowFilter) {
		m_rowFilter = rowFilter;
	}

	@Override
	public void setSelectColumns(ColumnMeta[] selectColumns) {
		m_selectColumns = selectColumns;
	}

	@Override
	public void setTables(List<TableProvider> tables) {
		m_tables = tables;
	}

	@Override
   public RowSet query() {
	   return null;
   }
}
