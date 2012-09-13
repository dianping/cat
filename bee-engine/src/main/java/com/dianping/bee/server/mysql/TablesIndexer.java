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
import com.dianping.bee.engine.spi.TableProvider;
import com.site.lookup.ContainerLoader;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class TablesIndexer implements Index {
	@Override
	public void query(RowContext ctx) throws Exception {
		List<DatabaseProvider> databases = ContainerLoader.getDefaultContainer().lookupList(DatabaseProvider.class);
		for (DatabaseProvider database : databases) {
			TableProvider[] tables = database.getTables();
			for (TableProvider table : tables) {
				applyRow(ctx, database, table);
			}
		}
	}

	/**
	 * @param ctx
	 * @param database
	 */
	private void applyRow(RowContext ctx, DatabaseProvider database, TableProvider table) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			TablesColumn column = ctx.getColumn(i);

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
			case TABLE_TYPE:
				if (database instanceof InformationSchemaDatabaseProvider) {
					ctx.setColumnValue(i, "SYSTEM VIEW");
				} else {
					ctx.setColumnValue(i, "BASE TABLE");
				}
				break;
			case ENGINE:
				ctx.setColumnValue(i, "MyISAM");
				break;
			case VERSION:
				ctx.setColumnValue(i, "10");
				break;
			case ROW_FORMAT:
				ctx.setColumnValue(i, "Dynamic");
				break;
			case TABLE_ROWS:
				ctx.setColumnValue(i, "0");
				break;
			case AVG_ROW_LENGTH:
				ctx.setColumnValue(i, "0");
				break;
			case DATA_LENGTH:
				ctx.setColumnValue(i, "0");
				break;
			case MAX_DATA_LENGTH:
				ctx.setColumnValue(i, "281474976710655");
				break;
			case INDEX_LENGTH:
				ctx.setColumnValue(i, "1024");
				break;
			case DATA_FREE:
				ctx.setColumnValue(i, "0");
				break;
			case AUTO_INCREMENT:
				break;
			case CREATE_TIME:
				break;
			case UPDATE_TIME:
				break;
			case CHECK_TIME:
				break;
			case TABLE_COLLATION:
				break;
			case CHECKSUM:
				break;
			case CREATE_OPTIONS:
				break;
			case TABLE_COMMENT:
				ctx.setColumnValue(i, "Bee Default Value, Don't be serious.");
				break;
			default:
			}
		}

		ctx.applyRow();
	}
}
