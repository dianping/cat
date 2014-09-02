package com.dianping.cat.report.analyzer;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.overload.CapacityUpdateTask;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class CapacityAnalyzer extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		ReportTaskBuilder builder = lookup(ReportTaskBuilder.class, CapacityUpdateTask.ID);
		
		builder.buildDailyTask("cat","cat", null);
	}

}
