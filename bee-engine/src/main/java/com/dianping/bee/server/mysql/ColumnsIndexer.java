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

import java.util.List;

import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.TableProvider;
import org.unidal.lookup.ContainerLoader;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class ColumnsIndexer implements Index {
	@Override
	public void query(RowContext ctx) throws Exception {
		List<DatabaseProvider> databases = ContainerLoader.getDefaultContainer().lookupList(DatabaseProvider.class);
		for (DatabaseProvider database : databases) {
			TableProvider[] tables = database.getTables();
			if (tables == null) {
				continue;
			}
			for (TableProvider table : tables) {
				ColumnMeta[] columns = table.getColumns();
				if (columns == null) {
					continue;
				}
				for (ColumnMeta column : columns) {
					applyRow(ctx, database, table, column);
				}
			}
		}
	}

	/**
	 * @param ctx
	 * @param database
	 */
	private void applyRow(RowContext ctx, DatabaseProvider database, TableProvider table, ColumnMeta columnMeta) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			ColumnsColumn column = ctx.getColumn(i);

			switch (column) {
			case TABLE_CATALOG:
				ctx.setColumnValue(i, "def");
				break;
			case TABLE_SCHEMA:
				ctx.setColumnValue(i, database.getName());
				break;
			case TABLE_NAME:
				ctx.setColumnValue(i, table.getName());
				break;
			case COLUMN_NAME:
				ctx.setColumnValue(i, columnMeta.getName());
				break;
			case ORDINAL_POSITION:
				ColumnMeta[] columns = table.getColumns();
				int position = 1;
				for (ColumnMeta col : columns) {
					if (col.equals(columnMeta)) {
						break;
					}
					position++;
				}
				ctx.setColumnValue(i, position);
				break;
			case COLUMN_DEFAULT:
				break;
			case IS_NULLABLE:
				break;
			case DATA_TYPE:
				String dataType = TypeUtils.convertFieldTypeToString(TypeUtils.convertJavaTypeToFieldType(columnMeta
				      .getType()));
				ctx.setColumnValue(i, dataType);
				break;
			case CHARACTER_MAXIMUM_LENGTH:
				break;
			case CHARACTER_OCTET_LENGTH:
				break;
			case NUMERIC_PRECISION:
				break;
			case NUMERIC_SCALE:
				break;
			case CHARACTER_SET_NAME:
				break;
			case COLLATION_NAME:
				break;
			case COLUMN_TYPE:
				// TODO need more precise
				String columnType = TypeUtils.convertFieldTypeToString(TypeUtils.convertJavaTypeToFieldType(columnMeta
				      .getType()));
				ctx.setColumnValue(i, columnType);
				break;
			case COLUMN_KEY:
				// TODO need more precise
				IndexMeta[] indexes = table.getIndexes();
				boolean isIndex = false;
				for (IndexMeta index : indexes) {
					for (int j = 0; j < index.getLength(); j++) {
						if (index.getColumn(j).equals(columnMeta)) {
							isIndex = true;
							break;
						}
					}
				}
				if (isIndex) {
					ctx.setColumnValue(i, "PRI");
				}
				break;
			case EXTRA:
				break;
			case PRIVILEGES:
				ctx.setColumnValue(i, "select,insert,update,references");
				break;
			case COLUMN_COMMENT:
				ctx.setColumnValue(i, "Bee Default Value, Don't be serious.");
				break;
			default:
			}
		}

		ctx.applyRow();
	}
}
