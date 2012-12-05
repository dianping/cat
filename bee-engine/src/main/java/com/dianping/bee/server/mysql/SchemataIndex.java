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
public class SchemataIndex extends AbstractIndexMeta<SchemataColumn> implements IndexMeta {
	public static final SchemataIndex IDX_NAME = new SchemataIndex(SchemataColumn.SCHEMA_NAME, true);

	private SchemataIndex(Object... args) {
		super(args);
	}

	@Override
	public Class<? extends Index> getIndexClass() {
		if (this == IDX_NAME) {
			return SchemataIndexer.class;
		} else {
			throw new UnsupportedOperationException("No index defined for index: " + this);
		}
	}

	public static SchemataIndex[] values() {
		return new SchemataIndex[] { IDX_NAME };
	}
}
