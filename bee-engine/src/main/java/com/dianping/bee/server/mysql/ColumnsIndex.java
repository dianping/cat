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
public class ColumnsIndex extends AbstractIndexMeta<TablesColumn> implements IndexMeta {
	public static final ColumnsIndex IDX_NAME = new ColumnsIndex(ColumnsColumn.COLUMN_NAME, true);

	private ColumnsIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index> getIndexClass() {
		if (this == IDX_NAME) {
			return ColumnsIndexer.class;
		} else {
			throw new UnsupportedOperationException("No index defined for index: " + this);
		}
	}

	public static ColumnsIndex[] values() {
		return new ColumnsIndex[] { IDX_NAME };
	}
}
