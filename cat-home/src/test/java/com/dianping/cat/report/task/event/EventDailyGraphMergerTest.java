package com.dianping.cat.report.task.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.task.EventMerger;

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
		EventReport report = m_meger.mergeForDaily(m_reportDomain, reports, m_domains, 1);
		String expeted = Files.forIO().readFrom(getClass().getResourceAsStream("EventMergerDaily.xml"), "utf-8");

		Assert.assertEquals(expeted.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
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
