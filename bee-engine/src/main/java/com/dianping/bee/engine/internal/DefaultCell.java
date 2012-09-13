package com.dianping.bee.engine.internal;

import com.dianping.bee.engine.Cell;
import com.dianping.bee.engine.spi.ColumnMeta;

public class DefaultCell implements Cell {
	private ColumnMeta m_columnMeta;

	private Object m_value;

	public DefaultCell(ColumnMeta columnMeta, Object value) {
		m_columnMeta = columnMeta;
		m_value = value;
	}

	@Override
	public ColumnMeta getMeta() {
		return m_columnMeta;
	}

	@Override
	public Object getValue() {
		return m_value;
	}

	public String toString() {
		return m_value.toString();
	}
}
