package com.dianping.cat.report.task.heartbeat;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class HeartBeatReportBuilderTest extends ComponentTestCase {

	//@Test
	public void testDailyTask() {
		ReportTaskBuilder builder = lookup(ReportTaskBuilder.class, HeartbeatAnalyzer.ID);

		builder.buildDailyTask("heartbeat", "cat", TimeHelper.getYesterday());
	}

}
