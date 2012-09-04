package com.dianping.bee.engine.spi.meta;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.index.Index;

public abstract class AbstractIndexMeta<T extends ColumnMeta> implements IndexMeta {
	private List<T> m_columns;

	private boolean[] m_orders;

	@SuppressWarnings("unchecked")
	protected AbstractIndexMeta(Object... args) {
		int length = args.length;

		if (length % 2 != 0) {
			throw new IllegalArgumentException(String.format("Parameters should be paired for %s!", getClass()));
		}

		m_columns = new ArrayList<T>(length / 2);
		m_orders = new boolean[length / 2];

		for (int i = 0; i < length / 2; i++) {
			m_columns.add((T) args[2 * i]);
			m_orders[i] = (Boolean) args[2 * i + 1];
		}
	}

	@Override
	public T getColumn(int index) {
		if (index >= 0 && index < m_columns.size()) {
			return m_columns.get(index);
		} else {
			throw new IndexOutOfBoundsException("size: " + m_columns.size() + ", index: " + index);
		}
	}

	@Override
	public int getLength() {
		return m_columns.size();
	}

	@Override
	public boolean isAscend(int index) {
		if (index >= 0 && index < m_orders.length) {
			return m_orders[index];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_orders.length + ", index: " + index);
		}
	}

	@Override
	public Class<? extends Index> getIndexClass() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}