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
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.TableProvider;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public enum InformationSchemaTableProvider implements TableProvider {
	CHARACTER_SETS("CHARACTER_SETS"),

	COLLATIONS("COLLATIONS"),

	COLLATION_CHARACTER_SET_APPLICABILITY("COLLATION_CHARACTER_SET_APPLICABILITY"),

	COLUMNS("COLUMNS", ColumnsColumn.values(), ColumnsIndex.values()),

	COLUMN_PRIVILEGES("COLUMN_PRIVILEGES"),

	ENGINES("ENGINES"),

	EVENTS("EVENTS"),

	FILES("FILES"),

	GLOBAL_STATUS("GLOBAL_STATUS"),

	GLOBAL_VARIABLES("GLOBAL_VARIABLES"),

	INNODB_CMP("INNODB_CMP"),

	INNODB_CMPMEM("INNODB_CMP"),

	INNODB_CMPMEM_REST("INNODB_CMP"),

	INNODB_LOCKS("INNODB_LOCKS"),

	INNODB_LOCK_WAITS("INNODB_LOCK_WAITS"),

	INNODB_TRX("INNODB_TRX"),

	KEY_COLUMN_USAGE("KEY_COLUMN_USAGE"),

	PARAMETERS("PARAMETERS"),

	PARTITIONS("PARTITIONS"),

	PLUGINS("PLUGINS"),

	PROCESSLIST("PROCESSLIST"),

	PROFILING("PROFILING"),

	REFERENTIAL_CONSTRAINTS("REFERENTIAL_CONSTRAINTS"),

	ROUTINES("ROUTINES"),

	SCHEMATA("SCHEMATA", SchemataColumn.values(), SchemataIndex.values()),

	SCHEMA_PRIVILEGES("SCHEMA_PRIVILEGES"),

	SESSION_STATUS("SESSION_STATUS"),

	SESSION_VARIABLES("SESSION_VARIABLES"),

	STATISTICS("STATISTICS"),

	TABLES("TABLES", TablesColumn.values(), TablesIndex.values()),

	TABLESPACES("TABLESPACES"),

	TABLE_CONSTRAINTS("TABLE_CONSTRAINTS"),

	TABLE_PRIVILEGES("TABLE_PRIVILEGES"),

	TRIGGERS("TRIGGERS"),

	USER_PRIVILEGES("USER_PRIVILEGES"),

	VIEWS("VIEWS");

	private String m_name;

	private ColumnMeta[] m_columns;

	private IndexMeta m_defaultIndex;

	private IndexMeta[] m_indexes;

	private InformationSchemaTableProvider(String name) {
		m_name = name;
	}

	private InformationSchemaTableProvider(String name, ColumnMeta[] columns, IndexMeta[] indexes) {
		m_name = name;
		m_columns = columns;
		m_defaultIndex = indexes.length > 0 ? indexes[0] : null;
		m_indexes = indexes;
	}

	@Override
	public ColumnMeta[] getColumns() {
		return m_columns;
	}

	@Override
	public IndexMeta getDefaultIndex() {
		if (m_defaultIndex == null) {
			throw new RuntimeException("No default index defined yet!");
		} else {
			return m_defaultIndex;
		}
	}

	@Override
	public IndexMeta[] getIndexes() {
		return m_indexes;
	}

	@Override
	public String getName() {
		return m_name;
	}
}