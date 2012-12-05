/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-3
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
package com.dianping.bee.server.mysql;

import com.dianping.bee.engine.spi.ColumnMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public enum ColumnsColumn implements ColumnMeta {
	TABLE_CATALOG(String.class),

	TABLE_SCHEMA(String.class),

	TABLE_NAME(String.class),

	COLUMN_NAME(String.class),

	ORDINAL_POSITION(Long.class),

	COLUMN_DEFAULT(String.class),

	IS_NULLABLE(String.class),

	DATA_TYPE(String.class),

	CHARACTER_MAXIMUM_LENGTH(Long.class),

	CHARACTER_OCTET_LENGTH(Long.class),

	NUMERIC_PRECISION(Long.class),

	NUMERIC_SCALE(Long.class),

	CHARACTER_SET_NAME(String.class),

	COLLATION_NAME(String.class),

	COLUMN_TYPE(String.class),

	COLUMN_KEY(String.class),

	EXTRA(String.class),

	PRIVILEGES(String.class),

	COLUMN_COMMENT(String.class);

	private String m_name;

	private Class<?> m_type;

	private ColumnsColumn(Class<?> type) {
		m_type = type;
		m_name = name().toLowerCase();
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public Class<?> getType() {
		return m_type;
	}
}
