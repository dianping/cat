package com.dianping.cat.report.task.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;

public class EventDailyGraphMergerTest {
	private EventMerger m_meger = new EventMerger();

	private Set<String> m_domains = new HashSet<String>();

	private String m_reportDomain = "MobileApi";

	List<EventReport> reports = new ArrayList<EventReport>();

	@Before
	public void setUp() {
		m_domains.add("MobileApi");
		m_domains.add("MobileApi1");
		for (int i = 0; i < 5; i++) {
			reports.add(creatReport());
		}
	}

	@Test
	public void testForMergerDaily() throws Exception {
		EventReport report = m_meger.mergeForDaily(m_reportDomain, reports, m_domains);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("EventMergerDaily.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}

	@Test
	public void testForMegerGraph() throws Exception {
		EventReport report = m_meger.mergeForGraph(m_reportDomain, reports);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("EventMergerGraph.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}

	private EventReport creatReport() {
		EventReport result = new EventReport();
		try {
			String xml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseEventReport.xml"), "utf-8");
			
			return DefaultSaxParser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
