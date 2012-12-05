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

import com.dianping.bee.engine.spi.AbstractIndexMeta;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.IndexMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class TablesIndex extends AbstractIndexMeta<TablesColumn> implements IndexMeta {
	public static final TablesIndex IDX_NAME = new TablesIndex(TablesColumn.TABLE_SCHEMA, true);

	private TablesIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index> getIndexClass() {
		if (this == IDX_NAME) {
			return TablesIndexer.class;
		} else {
			throw new UnsupportedOperationException("No index defined for index: " + this);
		}
	}

	public static TablesIndex[] values() {
		return new TablesIndex[] { IDX_NAME };
	}
}
