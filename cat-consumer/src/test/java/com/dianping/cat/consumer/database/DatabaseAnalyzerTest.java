package com.dianping.cat.consumer.database;

import junit.framework.Assert;

import org.junit.Test;

public class DatabaseAnalyzerTest {

	@Test
	public void testParseSimpleTableName() {
		DatabaseAnalyzer analyzer = new DatabaseAnalyzer();
		String simpleSelect = "select * from table";
		String simpleUpdate = " update table set col =1";
		String simpleInsert = " insert	into table (column) values(1)";
		String simpleDelete = "	delete	from table";

		Assert.assertEquals("table", analyzer.getTableNamesBySql(simpleSelect));
		Assert.assertEquals("table", analyzer.getTableNamesBySql(simpleUpdate));
		Assert.assertEquals("table", analyzer.getTableNamesBySql(simpleInsert));
		Assert.assertEquals("table", analyzer.getTableNamesBySql(simpleDelete));

	}
}