package com.dianping.cat.report.page.event;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.transform.EventTrendGraphBuilder;
import com.dianping.cat.report.page.event.transform.EventTrendGraphBuilder.EventReportVisitor;

public class EventTrendGraphBuilderTest {
	
	@Test
	public void testVisitName() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportDailyGraph.xml"),
		      "utf-8");
		EventReport report = DefaultSaxParser.parse(xml);

		EventReportVisitor visitor = new EventTrendGraphBuilder().new EventReportVisitor("10.1.77.193", "URL", "ClientInfo");
		visitor.visitEventReport(report);
		
		Map<String, double[]> datas = visitor.getDatas();
		assertArray(1725, datas.get(EventTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(EventTrendGraphBuilder.FAIL));
	}
	
	@Test
	public void testVisitType() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportDailyGraph.xml"),
		      "utf-8");
		EventReport report = DefaultSaxParser.parse(xml);

		EventReportVisitor visitor = new EventTrendGraphBuilder().new EventReportVisitor("10.1.77.193", "URL", "");
		visitor.visitEventReport(report);
		
		Map<String, double[]> datas = visitor.getDatas();
		assertArray(3450, datas.get(EventTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(EventTrendGraphBuilder.FAIL));
	}
	
	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
	}

}
