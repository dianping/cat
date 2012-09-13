/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-13
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
package com.dianping.bee.engine;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class ExceptionTest extends ComponentTestCase {
	protected String getCustomConfigurationName() {
		return TestEnvConfig.class.getName().replace('.', '/') + ".xml";
	}

	@Test
	public void testBadSQLException() throws Exception {
		String database = "cat";
		String sql = "select type, sum(failures) where domain='MobileApi' from transaction ";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		try {
			service.query(sql);
		} catch (SQLException e) {
			Assert.assertEquals(
			      "Error when querying with SQL(select type, sum(failures) where domain='MobileApi' from transaction )!",
			      "Error when querying with SQL(" + sql + ")!");
		}
	}
}
