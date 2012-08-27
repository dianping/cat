package com.dianping.cat.data.event;

import com.dianping.bee.engine.spi.meta.Index;

public enum EventIndex implements Index {
	IDX_STARTTIME_DOMAIN(EventColumn.StartTime, false, EventColumn.Domain, true);

	private EventColumn[] m_columns;

	private boolean[] m_orders;

	private EventIndex(Object... args) {
		int length = args.length;

		if (length % 2 != 0) {
			throw new IllegalArgumentException(String.format("Parameters should be paired for %s(%s)!", getClass(), name()));
		}

		m_columns = new EventColumn[length / 2];
		m_orders = new boolean[length / 2];

		for (int i = 0; i < length / 2; i++) {
			m_columns[i] = (EventColumn) args[2 * i];
			m_orders[i] = (Boolean) args[2 * i + 1];
		}
	}

	@Override
	public EventColumn getColumn(int index) {
		if (index >= 0 && index < m_columns.length) {
			return m_columns[index];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_columns.length + ", index: " + index);
		}
	}

	@Override
	public int getLength() {
		return m_columns.length;
	}

	@Override
	public boolean isAscend(int index) {
		if (index >= 0 && index < m_orders.length) {
			return m_orders[index];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_orders.length + ", index: " + index);
		}
	}
}