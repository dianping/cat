package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionDailyGraphMergerTest {
	private TransactionMerger m_meger = new TransactionMerger();

	private Set<String> m_domains = new HashSet<String>();

	private String m_reportDomain = "MobileApi";

	List<Report> reports = new ArrayList<Report>();

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
		TransactionReport report = m_meger.mergeForDaily(m_reportDomain, reports, m_domains);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionMergerDaily.xml"), "utf-8");
		//Assert.assertEquals(expeted, report.toString());

		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	@Test
	public void testForMegerGraph() throws Exception {
		TransactionReport report = m_meger.mergeForGraph(m_reportDomain, reports);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionMergerGraph.xml"), "utf-8");
		//Assert.assertEquals(expeted, report.toString());
		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	private Report creatReport() {
		Report result = new Report();
		try {
			String xml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseTransactionReport.xml"), "utf-8");

			result.setContent(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
