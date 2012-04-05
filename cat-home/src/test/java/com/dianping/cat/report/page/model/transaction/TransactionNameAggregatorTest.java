package com.dianping.cat.report.page.model.transaction;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class TransactionNameAggregatorTest {
	@Test
	public void test() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report = parser.parse(source);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("transaction-names.xml"), "utf-8");
		TransactionName all = new TransactionNameAggregator(report).mergesFor("URL");

		Assert.assertEquals(expected.replace("\r", ""), all.toString().replace("\r", ""));
	}
}
