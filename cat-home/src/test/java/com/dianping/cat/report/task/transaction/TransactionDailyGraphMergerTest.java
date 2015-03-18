package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.task.TransactionMerger;

public class TransactionDailyGraphMergerTest {
	private TransactionMerger m_meger = new TransactionMerger();

	private Set<String> m_domains = new HashSet<String>();

	private String m_reportDomain = "MobileApi";

	private List<TransactionReport> reports = new ArrayList<TransactionReport>();

	@Before
	public void setUp() {
		m_domains.add("MobileApi");
		m_domains.add("TuangouApi");
		for (int i = 0; i < 5; i++) {
			reports.add(creatReport());
		}
	}

	@Test
	public void testForMergerDaily() throws Exception {
		TransactionReport report = m_meger.mergeForDaily(m_reportDomain, reports, m_domains, 1);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionMergerDaily.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	private TransactionReport creatReport() {
		TransactionReport result = new TransactionReport();
		try {
			String xml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseTransactionReport.xml"), "utf-8");

			return DefaultSaxParser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
