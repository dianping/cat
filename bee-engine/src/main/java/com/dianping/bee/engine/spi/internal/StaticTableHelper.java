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
package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class StaticTableHelper {

	public static ColumnMeta findColumn(TableProvider table, String columnName) {
		ColumnMeta[] columns = table.getColumns();

		if (columns != null) {
			for (ColumnMeta column : columns) {
				if (column.getName().equalsIgnoreCase(columnName)) {
					return column;
				}
			}
		}
		throw new BadSQLSyntaxException("Column(%s) of table(%s) is not found!", columnName, table.getName());
	}

	public IndexMeta findIndex(TableProvider table, List<ColumnMeta> columns) {
		IndexMeta[] indexes = table.getIndexes();

		if (indexes != null && indexes.length > 0) {
			for (IndexMeta index : indexes) {
				// if first column of index is in columns, then pick it up
				ColumnMeta first = index.getColumn(0);
				String columnName = first.getName();

				for (ColumnMeta column : columns) {
					if (column.getName().equalsIgnoreCase(columnName)) {
						return index;
					}
				}
			}
		}

		return null;
	}
}
