package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.HashMap;
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

	private ColumnMeta[] m_columns;

	private List<SelectField> m_fields;

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
		return m_fields.get(colIndex);
	}

	@Override
	public int getColumnSize() {
		return m_fields.size();
	}

	@Override
	public IndexMeta getIndexMeta() {
		return m_index;
	}

	@Override
	public RowSet query() {
		Index index = lookup(m_index.getIndexClass());
		RowListener listener = new DefaultRowListener(m_fields);

		listener.setRowFilter(m_rowFilter);
		ctx.setColumns(m_columns);
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

	public void setColumns(List<ColumnMeta> columns) {
		m_columns = columns.toArray(new ColumnMeta[0]);
	}

	public void setIndex(IndexMeta index) {
		m_index = index;
	}

	public void setRowFilter(RowFilter rowFilter) {
		m_rowFilter = rowFilter;
	}

	public void setSelectFields(List<SelectField> fields) {
		m_fields = fields;
	}
}
