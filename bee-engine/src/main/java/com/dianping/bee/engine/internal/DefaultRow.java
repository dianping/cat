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

import java.util.Arrays;

import com.dianping.bee.engine.Cell;
import com.dianping.bee.engine.Row;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DefaultRow implements Row {

	private Cell[] m_cells;

	public DefaultRow(Cell[] cells) {
		this.m_cells = cells;
	}

	@Override
	public Cell getCell(int colIndex) {
		return m_cells[colIndex];
	}

	@Override
	public int getColumnSize() {
		return m_cells.length;
	}

	public String toString() {
		return Arrays.toString(m_cells);
	}
}
