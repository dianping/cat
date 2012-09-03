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
package com.dianping.bee.server.is;

import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.server.is.schema.SchemataColumn;
import com.dianping.bee.server.is.schema.SchemataIndex;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public enum InformationSchemaTableProvider implements TableProvider {
	CHARACTER_SETS("CHARACTER_SETS"),

	COLLATIONS("COLLATIONS"),

	COLUMNS("COLUMNS"),

	ENGINES("ENGINES"),

	TABLES("TABLES"),

	SCHEMATA("SCHEMATA", SchemataColumn.values(), SchemataIndex.values());

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