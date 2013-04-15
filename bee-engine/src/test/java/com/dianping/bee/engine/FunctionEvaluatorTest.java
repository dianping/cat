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

import org.unidal.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class FunctionEvaluatorTest extends ComponentTestCase {

	protected String getCustomConfigurationName() {
		return TestEnvConfigurator.class.getName().replace('.', '/') + ".xml";
	}

	@Test
	public void testAvgEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select avg(failures) from transaction";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(1, rs1.getRowSize());

		String sql2 = "select failures from transaction";
		RowSet rs2 = service.query(sql2);
		long sum = 0;
		long count = 0;
		for (int i = 0; i < rs2.getRowSize(); i++) {
			count++;
			sum += Long.parseLong(rs2.getRow(i).getCell(0).getValue().toString());
		}
		Assert.assertEquals((sum / count), ((Number) (rs1.getRow(0).getCell(0).getValue())).longValue());
	}

	@Test
	public void testConcatEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select concat(domain, starttime) from transaction where domain = 'MobileApi' and starttime = '20120822'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(5, rs1.getRowSize());
		Assert.assertEquals("MobileApi20120822", rs1.getRow(0).getCell(0).getValue().toString());
	}

	@Test
	public void testCountEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select type, count(failures) from transaction";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(1, rs1.getRowSize());
		Assert.assertEquals("10", rs1.getRow(0).getCell(1).getValue().toString());

		String sql2 = "select count(*) from transaction";
		RowSet rs2 = service.query(sql2);
		Assert.assertEquals(1, rs2.getRowSize());
		Assert.assertEquals("10", rs2.getRow(0).getCell(0).getValue().toString());
	}

	@Test
	public void testMaxEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select max(failures) from transaction";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(1, rs1.getRowSize());

		String sql2 = "select failures from transaction";
		RowSet rs2 = service.query(sql2);
		long max = Long.MIN_VALUE;
		for (int i = 0; i < rs2.getRowSize(); i++) {
			long item = Long.parseLong(rs2.getRow(i).getCell(0).getValue().toString());
			if (item > max) {
				max = item;
			}
		}
		Assert.assertEquals(max, ((Number) (rs1.getRow(0).getCell(0).getValue())).longValue());
	}

	@Test
	public void testMinEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select min(failures) from transaction";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(1, rs1.getRowSize());

		String sql2 = "select failures from transaction";
		RowSet rs2 = service.query(sql2);
		long min = Long.MAX_VALUE;
		for (int i = 0; i < rs2.getRowSize(); i++) {
			long item = Long.parseLong(rs2.getRow(i).getCell(0).getValue().toString());
			if (item < min) {
				min = item;
			}
		}
		Assert.assertEquals(min, ((Number) (rs1.getRow(0).getCell(0).getValue())).longValue());

	}

	@Test
	public void testSumEvaluator() throws Exception {
		String database = "cat";
		String sql1 = "select sum(failures) from transaction";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs1 = service.query(sql1);
		Assert.assertEquals(1, rs1.getRowSize());

		String sql2 = "select failures from transaction";
		RowSet rs2 = service.query(sql2);
		long sum = 0;
		for (int i = 0; i < rs2.getRowSize(); i++) {
			sum += Long.parseLong(rs2.getRow(i).getCell(0).getValue().toString());
		}
		Assert.assertEquals(sum, ((Number) (rs1.getRow(0).getCell(0).getValue())).longValue());
	}
}
