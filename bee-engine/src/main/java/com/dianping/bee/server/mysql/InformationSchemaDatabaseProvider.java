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

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class InformationSchemaDatabaseProvider implements DatabaseProvider {
	public static final String ID = "information_schema";

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public TableProvider[] getTables() {
		return InformationSchemaTableProvider.values();
	}
}
