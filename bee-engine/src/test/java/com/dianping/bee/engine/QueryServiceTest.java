package com.dianping.bee.engine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class QueryServiceTest extends ComponentTestCase {
	@Test
	public void testStatement() throws Exception {
		String database = "cat";
		String sql = "select type, sum(failures) from transaction where domain='MobileApi' and starttime='20120822'";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs = service.query(sql);

		Assert.assertEquals(5, rs.getRowSize());
	}

	@Test
	public void testPreparedStatement() throws Exception {
		String database = "cat";
		String sql = "select type, sum(failures) from transaction where domain=? and starttime=?";
		QueryService service = lookup(QueryService.class);
		service.use(database);
		RowSet rs = service.query(sql, "MobileApi", "20120822");

		Assert.assertEquals(5, rs.getRowSize());
	}
}
