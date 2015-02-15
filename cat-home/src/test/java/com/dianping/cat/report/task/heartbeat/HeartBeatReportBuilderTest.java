package com.dianping.cat.report.task.heartbeat;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.task.TaskBuilder;

public class HeartBeatReportBuilderTest extends ComponentTestCase {

	//@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, HeartbeatAnalyzer.ID);

		builder.buildDailyTask("heartbeat", "cat", TimeHelper.getYesterday());
	}

}
