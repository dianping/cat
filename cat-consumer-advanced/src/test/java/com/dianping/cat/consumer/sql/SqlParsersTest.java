package com.dianping.cat.consumer.sql;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class SqlParsersTest {
	private void checkTables(String sql, String... expectedTables) {
		List<String> tables = SqlParsers.forTable().parse(sql);

		Assert.assertEquals(Arrays.asList(expectedTables).toString(), tables.toString());
	}

	@Test
	public void testParseTables() {
		checkTables("select * from table1", "table1");
		checkTables("update table2 set col =1", "table2");
		checkTables("insert	into table3 (column1) values(1)", "table3");
		checkTables("delete	from table4", "table4");

		checkTables("select r.id,r.name,r.des from resource r where r.id in "
		      + "(select resource_id from resource_role where role_id=?)", "resource", "resource_role");
	}
}
