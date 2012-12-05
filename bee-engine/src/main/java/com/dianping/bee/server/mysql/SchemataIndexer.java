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

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowContext;
import com.site.lookup.ContainerLoader;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SchemataIndexer implements Index {
	@Override
	public void query(RowContext ctx) throws Exception {
		List<DatabaseProvider> databases = ContainerLoader.getDefaultContainer().lookupList(DatabaseProvider.class);
		for (DatabaseProvider database : databases) {
			applyRow(ctx, database);
		}
	}

	/**
	 * @param ctx
	 * @param database
	 */
	private void applyRow(RowContext ctx, DatabaseProvider database) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			SchemataColumn column = ctx.getColumn(i);

			switch (column) {
			case CATALOG_NAME:
				ctx.setColumnValue(i, "def");
				break;
			case SCHEMA_NAME:
				ctx.setColumnValue(i, database.getName());
				break;
			case DEFAULT_CHARACTER_SET_NAME:
				ctx.setColumnValue(i, "utf8");
				break;
			case DEFAULT_COLLATION_NAME:
				ctx.setColumnValue(i, "utf8_general_ci");
				break;
			case SQL_PATH:
				break;
			default:
			}
		}

		ctx.applyRow();
	}
}
