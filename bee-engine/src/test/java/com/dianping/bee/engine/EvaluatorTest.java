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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class EvaluatorTest extends ComponentTestCase {

	protected String getCustomConfigurationName() {
		return TestEnvConfigurator.class.getName().replace('.', '/') + ".xml";
	}

	@Test
	public void testBetweenAndEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where starttime between '20120820' and '20120824'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(5, rs1.getRowSize());

		String sql2 = "select type, sum(failures) from transaction where starttime between '20120823' and '20120824'";
		RowSet rs2 = service.query(sql2);
		Assert.assertEquals(0, rs2.getRowSize());
	}

	@Test
	public void testComparisionIsEvluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where domain is NOT NULL";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(10, rs1.getRowSize());

		String sql2 = "select type, sum(failures) from transaction where domain is NULL";
		RowSet rs2 = service.query(sql2);
		Assert.assertEquals(0, rs2.getRowSize());
	}

	@Test
	public void testGreaterThanEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where starttime > '20120821'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertTrue(rs1.getRowSize() >= 5);
	}

	@Test
	public void testGreaterThanOrEqualsEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where starttime >= '20120822'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertTrue(rs1.getRowSize() >= 5);
	}

	@Test
	public void testInEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where domain in ('Mobile','Api','MobileApi')";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(5, rs1.getRowSize());

		String sql2 = "select type, sum(failures) from transaction where domain not in ('Mobile','Api','MobileApi')";
		RowSet rs2 = service.query(sql2);
		Assert.assertEquals(5, rs2.getRowSize());
	}

	@Test
	public void testLessThanEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where starttime < '20120823'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertTrue(rs1.getRowSize() >= 5);
	}

	@Test
	public void testLessThanOrEqualsEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where starttime <= '20120822'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertTrue(rs1.getRowSize() >= 5);
	}

	@Test
	public void testLogicOrEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, sum(failures) from transaction where domain ='MobileApi' or starttime='201208'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(5, rs1.getRowSize());

		String sql2 = "select type, sum(failures) from transaction where domain ='Mobile' or starttime='20120822'";
		RowSet rs2 = service.query(sql2);
		Assert.assertEquals(5, rs2.getRowSize());
	}
}
