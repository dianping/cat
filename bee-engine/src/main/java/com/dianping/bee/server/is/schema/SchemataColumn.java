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
package com.dianping.bee.server.is.schema;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public enum SchemataColumn implements ColumnMeta {
	CATALOG_NAME(String.class),

	SCHEMA_NAME(String.class),

	DEFAULT_CHARACTER_SET_NAME(String.class),

	DEFAULT_COLLATION_NAME(String.class),

	SQL_PATH(String.class);

	private String m_name;

	private Class<?> m_type;

	private SchemataColumn(Class<?> type) {
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
