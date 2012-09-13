package com.dianping.bee.engine.spi.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dianping.bee.engine.evaluator.Evaluator;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.RowListener;
import com.site.lookup.ContainerHolder;

public class DefaultRowContext extends ContainerHolder implements RowContext {
	private ColumnMeta[] m_columns;

	private Object[] m_parameters;

	private Object[] m_values;

	private RowListener m_listener;

	private Map<String, List<Object>> m_attributes;

	@Override
   public void afterQuery() {
		m_listener.onEnd(this);
   }

	@Override
	public void applyRow() {
		m_listener.onRow(this);
		Arrays.fill(m_values, null);
	}

	@Override
   public void beforeQuery() {
		m_listener.onBegin(this);
   }

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getAttributeValues(String name) {
		return (List<T>) m_attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ColumnMeta> T getColumn(int colIndex) {
		return (T) m_columns[colIndex];
	}

	@Override
	public int getColumnSize() {
		return m_columns.length;
	}

	public Evaluator<?, ?> getEvaluator(String name) {
		return lookup(Evaluator.class, name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFirstAttribute(String name, T defaultValue) {
		List<T> list = (List<T>) m_attributes.get(name);

		if (list == null || list.isEmpty()) {
			return defaultValue;
		} else {
			return list.get(0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getParameter(int colIndex) {
		return (T) m_parameters[colIndex];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int colIndex) {
		return (T) m_values[colIndex];
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(String columnName) {
		int len = m_columns.length;

		for (int i = 0; i < len; i++) {
			ColumnMeta column = m_columns[i];

			if (column.getName().equalsIgnoreCase(columnName)) {
				return (T) m_values[i];
			}
		}

		return null;
	}

	public void setAttributes(Map<String, List<Object>> attributes) {
		m_attributes = attributes;
	}

	@Override
	public void setColumns(ColumnMeta[] columns) {
		m_columns = columns;
		m_values = new Object[columns.length];
	}

	@Override
	public void setColumnValue(int colIndex, Object value) {
		m_values[colIndex] = value;
	}

	public void setParameters(Object[] parameters) {
		m_parameters = parameters;
	}

	public void setRowListener(RowListener listener) {
		m_listener = listener;
	}

	public String toString() {
		return Arrays.toString(m_values);
	}
}
