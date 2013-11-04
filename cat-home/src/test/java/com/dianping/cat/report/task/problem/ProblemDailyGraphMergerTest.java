package com.dianping.cat.report.task.problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemDailyGraphMergerTest {
	private ProblemMerger m_meger = new ProblemMerger();

	private Set<String> m_domains = new HashSet<String>();

	private String m_reportDomain = "MobileApi";

	private List<ProblemReport> reports = new ArrayList<ProblemReport>();

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
		ProblemReport report = m_meger.mergeForDaily(m_reportDomain, reports, m_domains);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMergerDaily.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	@Test
	public void testForMegerGraph() throws Exception {
		ProblemReport report = m_meger.mergeForGraph(m_reportDomain, reports);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMergerGraph.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	private ProblemReport creatReport() {
		try {
			String xml = Files.forIO().readFrom(getClass().getResourceAsStream("problemCreator.xml"), "utf-8");

			return DefaultSaxParser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
