/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-24
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
package com.dianping.bee.server;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class InformationSchemaDatabase implements DatabaseProvider {

	public static enum InformationSchemaTable implements TableProvider {
		CHARACTER_SETS("CHARACTER_SETS"),

		COLLATIONS("COLLATIONS"),

		COLUMNS("COLUMNS"),

		ENGINES("ENGINES"),

		TABLES("TABLES"),

		SCHEMATA("SCHEMATA") {
			@Override
			public SchemataColumn[] getColumns() {
				return SchemataColumn.values();
			}

		};

		private String m_name;

		private InformationSchemaTable(String name) {
			m_name = name;
		}

		@Override
		public ColumnMeta[] getColumns() {
			return SchemataColumn.values();
		}

		@Override
		public IndexMeta[] getIndexes() {
			return null;
		}

		@Override
		public String getName() {
			return m_name;
		}

//		@Override
//		public RowSet queryByIndex(IndexMeta index, ColumnMeta[] selectColumns) {
//			ColumnMeta[] columns = selectColumns;
//			DefaultRowSet rowSet = new DefaultRowSet(columns);
//
//			Cell[] cells = new Cell[columns.length];
//			for (int colIndex = 0; colIndex < cells.length; colIndex++) {
//				ColumnMeta columnMeta = columns[colIndex];
//				cells[colIndex] = new DefaultCell(columnMeta, null);
//			}
//
//			return rowSet;
//		}

		@Override
      public IndexMeta getDefaultIndex() {
	      // TODO Auto-generated method stub
	      return null;
      }
	}

	public static enum SchemataColumn implements ColumnMeta {
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

	@Override
	public String getName() {
		return "information_schema";
	}

	@Override
	public TableProvider[] getTables() {
		return InformationSchemaTable.values();
	}
}
