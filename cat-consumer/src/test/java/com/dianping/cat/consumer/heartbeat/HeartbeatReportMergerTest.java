package com.dianping.cat.consumer.heartbeat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;

public class HeartbeatReportMergerTest {
	@Test
	public void testHaertbeatReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat_analyzer_old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat_analyzer_old.xml"), "utf-8");
		HeartbeatReport reportOld = DefaultSaxParser.parse(oldXml);
		HeartbeatReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat_analyzer_merge.xml"), "utf-8");
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getHeartbeatReport().toString()
		      .replace("\r", ""));

	}
}
