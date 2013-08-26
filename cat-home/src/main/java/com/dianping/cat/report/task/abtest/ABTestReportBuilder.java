package com.dianping.cat.report.task.abtest;

import java.util.Date;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class ABTestReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Inject
	private AbtestReportDao m_abtestReportDao;

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		MetricReport metricReport = m_reportService.queryMetricReport(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_HOUR));

		MetricReportForABTestVisitor visitor = new MetricReportForABTestVisitor();

		metricReport.accept(visitor);

		Map<Integer, AbtestReport> result = visitor.getReportMap();

		Date now = new Date();
		for (AbtestReport report : result.values()) {
			com.dianping.cat.home.dal.abtest.AbtestReport _report = new com.dianping.cat.home.dal.abtest.AbtestReport();

			_report.setId(report.getRunId());
			_report.setCreationDate(now);
			_report.setPeriod(period);
			_report.setContent(report.toString());

			try {
				m_abtestReportDao.insert(_report);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}

		return true;
	}

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("ABTest report don't support daily report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("ABTest line report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new UnsupportedOperationException("ABTest line report don't support weekly report!");
	}
}
