package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.spi.ReportFacade;

public class RouterBuilderTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		ReportFacade reportFacade = (ReportFacade) lookup(ReportFacade.class);
		Task task = new Task();
		Date reportPeriod = new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-28");
		
		task.setReportName(Constants.REPORT_ROUTER);
		task.setReportPeriod(reportPeriod);
		task.setReportDomain(Constants.CAT);
		task.setTaskType(1);
		reportFacade.builderReport(task );
		
		ReportServiceManager manager = (ReportServiceManager) lookup(ReportServiceManager.class);
		RouterConfig report = manager.queryRouterConfigReport(Constants.CAT, reportPeriod, new Date(reportPeriod.getTime()+TimeUtil.ONE_DAY));
		
		System.err.println(report);
	}

}
