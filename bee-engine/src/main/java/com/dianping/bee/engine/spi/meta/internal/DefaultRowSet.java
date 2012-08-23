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

	public DefaultRowSet(List<ColumnMeta> columnMetas) {
		this.m_columnMetas = columnMetas;
		this.m_rows = new ArrayList<Row>();
	}

	private List<ColumnMeta> m_columnMetas;

	private List<Row> m_rows;

	public void addRow(Row row) {
		this.m_rows.add(row);
	}

	@Override
	public ColumnMeta getColumn(int colIndex) {
		return m_columnMetas.get(colIndex);
	}

	@Override
	public int getColumns() {
		return m_columnMetas.size();
	}

	@Override
	public Row getRow(int rowIndex) {
		return m_rows.get(rowIndex);
	}

	@Override
	public int getRows() {
		return m_rows.size();
	}

	@Override
	public void filter(RowFilter rowFilter) {
		Iterator<Row> it = m_rows.iterator();
		while (it.hasNext()) {
			Row row = it.next();
			if (rowFilter.filter(row)) {
				it.remove();
			}
		}
	}

}
