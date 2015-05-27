package com.dianping.cat.heartbeat;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.heartbeat.analyzer.HeartbeatAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.task.TaskBuilder;

public class HeartBeatReportBuilderTest extends ComponentTestCase {

	//@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, HeartbeatAnalyzer.ID);

		builder.buildDailyTask("heartbeat", "cat", TimeHelper.getYesterday());
	}

}
