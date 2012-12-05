package com.dianping.bee.engine.helper;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class SqlParsersTest {
	@Test
	public void testTables() {
		checkTables("select * from a", "[a]");
		checkTables("select * from a,b where c in (select * from d)", "[a, b, d]");
		checkTables("select * from a,b where c in (select * from a)", "[a, b]");
		checkTables("insert into x(a,b,c) values(1,2,3)", "[x]");
		checkTables("insert into x(a,b,c) select a,b,c from y", "[x, y]");
		checkTables("update x set a=1 where b=2", "[x]");
		checkTables("delete from x where b=2", "[x]");
	}

	private void checkTables(String sql, String expected) {
		List<String> tables = SqlParsers.forTable().parse(sql);

		Assert.assertEquals(expected, tables.toString());
	}
}
