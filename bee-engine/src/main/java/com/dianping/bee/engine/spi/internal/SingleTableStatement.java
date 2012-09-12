package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.RowListener;
import com.dianping.bee.engine.spi.Statement;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class SingleTableStatement extends ContainerHolder implements Statement {
	@Inject
	protected RowContext ctx;

	private IndexMeta m_index;

	private RowFilter m_rowFilter;

	private ColumnMeta[] m_selectColumns;

	private Map<ColumnMeta, Integer> m_allColumns = new LinkedHashMap<ColumnMeta, Integer>();

	private Map<String, List<Object>> m_attributes = new HashMap<String, List<Object>>();

	public void addAttribute(String name, Object value) {
		List<Object> list = m_attributes.get(name);

		if (list == null) {
			list = new ArrayList<Object>(3);
			m_attributes.put(name, list);
		}

		list.add(value);
	}

	@Override
	public ColumnMeta getColumnMeta(int colIndex) {
		if (colIndex >= 0 && colIndex < m_selectColumns.length) {
			return m_selectColumns[colIndex];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_selectColumns.length + ", index: " + colIndex);
		}
	}

	@Override
	public int getColumnSize() {
		return m_selectColumns.length;
	}

	@Override
	public IndexMeta getIndexMeta() {
		return m_index;
	}

	@Override
	public RowSet query() {
		Index index = lookup(m_index.getIndexClass());
		List<ColumnMeta> columns = new ArrayList<ColumnMeta>(m_allColumns.keySet());
		RowListener listener = new DefaultRowListener(m_selectColumns);
		listener.setRowFilter(m_rowFilter);

		ctx.setColumnMeta(columns.toArray(new ColumnMeta[0]));
		ctx.setRowListener(listener);
		ctx.setAttributes(m_attributes);

		try {
			index.query(ctx);

			return listener.getRowSet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			release(index);
		}
	}

	public void setIndex(IndexMeta index) {
		m_index = index;
	}

	public void setRowFilter(RowFilter rowFilter) {
		m_rowFilter = rowFilter;
	}

	public void setSelectColumns(List<ColumnMeta> selectColumns) {
		int len = selectColumns.size();
		ColumnMeta[] columns = new ColumnMeta[len];

		for (int i = 0; i < len; i++) {
			ColumnMeta column = selectColumns.get(i);

			columns[i] = column;
			m_allColumns.put(column, i);
		}

		m_selectColumns = columns;
	}

	public void setWhereColumns(List<ColumnMeta> whereColumns) {
		for (ColumnMeta column : whereColumns) {
			if (!m_allColumns.containsKey(column)) {
				m_allColumns.put(column, -1);
			}
		}
	}
}
