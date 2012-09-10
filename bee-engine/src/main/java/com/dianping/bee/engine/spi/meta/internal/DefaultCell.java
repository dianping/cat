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

import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DefaultCell implements Cell {

	private ColumnMeta m_columnMeta;

	private Object m_value;

	public DefaultCell(ColumnMeta columnMeta, Object value) {
		this.m_columnMeta = columnMeta;
		this.m_value = value;
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
