package com.dianping.cat.report.task.router;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.system.page.router.config.RouterConfigAdjustor;

public class RouterReportAdjustTest extends ComponentTestCase {

	@Test
	public void test() throws ParseException {
		RouterConfigAdjustor adjustor = lookup(RouterConfigAdjustor.class);
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-04-19 12:00:00");

		adjustor.Adjust(period);
	}

}
