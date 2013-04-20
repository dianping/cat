package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);

		TransactionType type = report.findMachine("10.1.77.193").findType("URL");

		int size = type.getNames().size();
		System.out.println("Name size :" + size);
		for (int i = 0; i < 3500; i++) {
			type.addName(new TransactionName("Test" + i));
		}

		System.out.println(report.toString().length());
		System.out.println("Name size :" + type.getNames().size());

		TransactionReportUrlFilter f1 = new TransactionReportUrlFilter();
		String filterReport = f1.buildXml(report);
		TransactionReport newReport = DefaultSaxParser.parse(filterReport);

		System.out.println(newReport.toString().length());

		int newSize = newReport.findMachine("10.1.77.193").findType("URL").getNames().size();

		Assert.assertEquals(199, newSize);

		String url = "/topic/341739¬g&quot;";

		size = url.length();
		for (int i = 0; i < size; i++) {
			if (url.charAt(i) > 255 || url.charAt(i) < 0) {
				System.out.println(i + " " + url.charAt(i));
			}
		}
	}
}
