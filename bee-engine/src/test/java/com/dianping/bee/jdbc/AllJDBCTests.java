/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-11
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
package com.dianping.bee.jdbc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.bee.mysql.InformationSchemaTest;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(Suite.class)
@SuiteClasses({

StatementTest.class,

PreparedStatementTest.class,

InformationSchemaTest.class

})
public class AllJDBCTests {

}