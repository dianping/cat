package com.dianping.cat.report.task.router;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class RouterReportBuilderTest extends ComponentTestCase {

	@Test
	public void test() throws ParseException {
		RouterConfigBuilder builder = (RouterConfigBuilder) lookup(TaskBuilder.class, RouterConfigBuilder.ID);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-11-03 00:00:00");

		builder.buildDailyTask(Constants.REPORT_ROUTER, "cat", period);
	}

	@Test
	public void test2() throws ParseException {
		RouterConfigHandler handler = (RouterConfigHandler) lookup(RouterConfigHandler.class);
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-04-20 00:00:00");

		RouterConfig routerConfig = handler.buildRouterConfig("cat", period);

		System.out.println(routerConfig);

	}
}
