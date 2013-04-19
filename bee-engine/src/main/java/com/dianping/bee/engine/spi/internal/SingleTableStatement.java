package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.RowListener;
import com.dianping.bee.engine.spi.Statement;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

public class SingleTableStatement extends ContainerHolder implements Statement, LogEnabled {
	@Inject
	protected RowContext ctx;

	private String m_sql;

	private IndexMeta m_index;

	private RowFilter m_rowFilter;

	private ColumnMeta[] m_columns;

	private List<SelectField> m_fields;

	private Map<String, List<Object>> m_attributes = new HashMap<String, List<Object>>();

	private Logger m_logger;

	public void addAttribute(String name, Object value) {
		List<Object> list = m_attributes.get(name);

		if (list == null) {
			list = new ArrayList<Object>(3);
			m_attributes.put(name, list);
		}

		list.add(value);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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
	public String getSQL() {
		return m_sql;
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
			ctx.beforeQuery();
			index.query(ctx);

			return listener.getRowSet();
		} catch (Exception e) {
			m_logger.error(String.format("Error when handling query(%s)!", this), e);
			throw new RuntimeException(e);
		} finally {
			ctx.afterQuery();
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

	public void setSQL(String sql) {
		m_sql = sql;
	}
}
