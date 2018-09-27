package com.dianping.cat.consumer.storage;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.DatabaseParser.Database;

public class DatabaseParserTest extends ComponentTestCase {

	@Test
	public void testOracle() {
		DatabaseParser parser = lookup(DatabaseParser.class);
		Database database = parser.parseDatabase("jdbc:oracle:thin:@ 172.20.70.36:1521:gbst");

		Assert.assertEquals("172.20.70.36", database.getIp());
		Assert.assertEquals("gbst", database.getName());
	}

	@Test
	public void testMysql() {
		DatabaseParser parser = lookup(DatabaseParser.class);
		Database database = parser.parseDatabase("jdbc:mysql://localhost:3306/mydb");

		Assert.assertEquals("localhost", database.getIp());
		Assert.assertEquals("mydb", database.getName());
	}

}
