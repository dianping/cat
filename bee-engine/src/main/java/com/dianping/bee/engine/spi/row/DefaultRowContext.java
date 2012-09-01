package com.dianping.bee.engine.spi.row;

import java.util.Arrays;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public class DefaultRowContext implements RowContext {
	private ColumnMeta[] m_columns;

	private Object[] m_values;

	private RowListener m_listener;

	public DefaultRowContext(ColumnMeta[] columns) {
		m_columns = columns;
		m_values = new Object[columns.length];
	}

	@Override
	public void apply() {
		m_listener.onRow(this);
		Arrays.fill(m_values, null);
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int colIndex) {
		return (T) m_values[colIndex];
	}

	@Override
	public void setColumnValue(int colIndex, Object value) {
		m_values[colIndex] = value;
	}

	@Override
	public void setRowListener(RowListener listener) {
		m_listener = listener;
	}

	@Override
	public Object getValue(String columnName) {
		int len = m_columns.length;

		for (int i = 0; i < len; i++) {
			ColumnMeta column = m_columns[i];

			if (column.getName().equalsIgnoreCase(columnName)) {
				return m_values[i];
			}
		}

		return null;
	}
}
