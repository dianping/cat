package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.spi.ReportFacade;

public class RouterBuilderTest extends ComponentTestCase {

	public String day1 = "2014-07-25";

	public String day2 = "2014-07-26";

	public String day3 = "2014-07-27";

	public String day4 = "2014-07-28";

	@Test
	public void test() throws Exception {
		ReportFacade reportFacade = (ReportFacade) lookup(ReportFacade.class);
		Task task = new Task();
		Date reportPeriod = new SimpleDateFormat("yyyy-MM-dd").parse(day3);

		task.setReportName(Constants.REPORT_ROUTER);
		task.setReportPeriod(reportPeriod);
		task.setReportDomain(Constants.CAT);
		task.setTaskType(1);
		reportFacade.builderReport(task);

		task.setReportPeriod(new SimpleDateFormat("yyyy-MM-dd").parse(day4));
		reportFacade.builderReport(task);
	}

	@Test
	public void test1() throws Exception {
		ReportServiceManager service = lookup(ReportServiceManager.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		RouterConfig report1 = service.queryRouterConfigReport(Constants.CAT, sdf.parse(day3), sdf.parse(day4));

		RouterConfig report2 = service.queryRouterConfigReport(Constants.CAT, sdf.parse(day3), sdf.parse(day4));

		Assert.assertEquals(report1.toString(), report2.toString());
	}
	
}
