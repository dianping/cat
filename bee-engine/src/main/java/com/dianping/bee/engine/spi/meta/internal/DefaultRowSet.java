/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.engine.spi.meta.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DefaultRowSet implements RowSet {

	private ColumnMeta[] m_columnMetas;

	private List<Row> m_rows;

	public DefaultRowSet(ColumnMeta[] columnMetas) {
		this.m_columnMetas = columnMetas;
		this.m_rows = new ArrayList<Row>();
	}

	public void addRow(Row row) {
		this.m_rows.add(row);
	}

	@Override
	public void filter(RowFilter rowFilter) {
		if (rowFilter == null)
			return;
		Iterator<Row> it = m_rows.iterator();
		while (it.hasNext()) {
			Row row = it.next();
			if (!rowFilter.filter(row)) {
				it.remove();
			}
		}
	}

	@Override
	public ColumnMeta getColumn(int colIndex) {
		if (colIndex >= 0 && colIndex < m_columnMetas.length) {
			return m_columnMetas[colIndex];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_columnMetas.length + ", index: " + colIndex);
		}
	}

	@Override
	public int getColumnSize() {
		return m_columnMetas.length;
	}

	@Override
	public Row getRow(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < m_rows.size()) {
			return m_rows.get(rowIndex);
		} else {
			throw new IndexOutOfBoundsException("size: " + m_rows.size() + ", index: " + rowIndex);
		}
	}

	@Override
	public int getRowSize() {
		return m_rows.size();
	}
}
