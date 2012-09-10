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
package com.dianping.bee.engine.internal;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.Cell;
import com.dianping.bee.engine.Row;
import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.spi.ColumnMeta;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		int cols = m_columnMetas.length;

		for (int i = 0; i < cols; i++) {
			ColumnMeta column = m_columnMetas[i];

			sb.append(column.getName()).append('|');
		}

		sb.append('\n');

		int rows = m_rows.size();

		for (int i = 0; i < rows; i++) {
			Row row = m_rows.get(i);

			for (int j = 0; j < cols; j++) {
				Cell cell = row.getCell(j);

				sb.append(cell.getValue()).append('|');
			}

			sb.append('\n');
		}

		sb.append(rows).append(" rows selected.");

		return sb.toString();
	}
}
