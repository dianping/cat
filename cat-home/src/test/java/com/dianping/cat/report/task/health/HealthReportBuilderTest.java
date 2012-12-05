package com.dianping.cat.report.task.health;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.transform.DefaultSaxParser;

public class HealthReportBuilderTest {

	@Test
	public void testBuildHealthReport() throws Exception {

		String report0 = Files.forIO().readFrom(getClass().getResourceAsStream("HealthReport0.xml"), "utf-8");
		String report1 = Files.forIO().readFrom(getClass().getResourceAsStream("HealthReport1.xml"), "utf-8");
		String report2 = Files.forIO().readFrom(getClass().getResourceAsStream("HealthReport2.xml"), "utf-8");

		HealthReport healthReport0 = DefaultSaxParser.parse(report0);
		HealthReport healthReport1 = DefaultSaxParser.parse(report1);
		HealthReport healthReport2 = DefaultSaxParser.parse(report2);

		HealthReportMerger merger = new HealthReportMerger(new HealthReport("OpenWeb"));
		merger.setDuration(100);
		healthReport0.accept(merger);
		healthReport1.accept(merger);
		healthReport2.accept(merger);

		HealthReport result = merger.getHealthReport();
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ResultReport.xml"), "utf-8");
		
		Assert.assertEquals(expected.replaceAll("\\s*", ""), result.toString().replaceAll("\\s*", ""));
		//Assert.assertEquals(expected.replaceAll("\r", ""), result.toString().replaceAll("\r", ""));
	}
}
