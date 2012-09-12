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

import java.sql.Date;

import com.dianping.bee.engine.spi.ColumnMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public enum TablesColumn implements ColumnMeta {
	TABLE_CATALOG(String.class),

	TABLE_SCHEMA(String.class),

	TABLE_NAME(String.class),

	TABLE_TYPE(String.class),

	ENGINE(String.class),
	
	VERSION(Long.class),

	ROW_FORMAT(String.class),
	
	TABLE_ROWS(Long.class),
	
	AVG_ROW_LENGTH(Long.class),
	
	DATA_LENGTH(Long.class),
	
	MAX_DATA_LENGTH(Long.class),
	
	INDEX_LENGTH(Long.class),
	
	DATA_FREE(Long.class),
	
	AUTO_INCREMENT(Long.class),
	
	CREATE_TIME(Date.class),
	
	UPDATE_TIME(Date.class),
	
	CHECK_TIME(Date.class),
	
	TABLE_COLLATION(String.class),
	
	CHECKSUM(Long.class),
	
	CREATE_OPTIONS(String.class),
	
	TABLE_COMMENT(String.class);
	
	private String m_name;

	private Class<?> m_type;

	private TablesColumn(Class<?> type) {
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
