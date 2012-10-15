package com.dianping.cat.system.notify;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.site.lookup.ComponentTestCase;

public class TransactionRenderTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		ReportRender render = lookup(ReportRender.class);
		String excepted = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionRender.txt"), "utf-8");
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("Transaction.xml"), "utf-8");
		TransactionReport report = new DefaultDomParser().parse(oldXml);

		String result = render.renderReport(report);
		Assert.assertEquals(excepted.replaceAll("\\s*", ""), result.replaceAll("\\s*", ""));

	}
}
