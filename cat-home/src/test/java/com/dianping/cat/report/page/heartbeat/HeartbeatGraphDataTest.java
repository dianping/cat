package com.dianping.cat.report.page.heartbeat;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.heartbeat.HeartbeatGraphCreator;

@RunWith(JUnit4.class)
public class HeartbeatGraphDataTest extends ComponentTestCase {

	@Test
	public void testModel() throws Exception {
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat.xml"), "utf-8");
		HeartbeatReport reportOld = DefaultSaxParser.parse(newXml);
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportOld.getDomain()));

		reportOld.accept(merger);
		Assert.assertEquals(newXml.replaceAll("\r", ""), reportOld.toString().replaceAll("\r", ""));
	}

	@Test
	public void test1() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat.xml"), "utf-8");
		HeartbeatReport report = DefaultSaxParser.parse(xml);
		HeartbeatGraphCreator creator = new HeartbeatGraphCreator();
		List<Graph> graphs = creator.splitReportToGraphs(report.getStartTime(), report.getDomain(), "heartbeat", report);
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("result"), "utf-8");

		Assert.assertEquals(result, graphs.get(0).getDetailContent());
	}
}
