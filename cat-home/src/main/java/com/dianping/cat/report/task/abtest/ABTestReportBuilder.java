package com.dianping.cat.report.task.abtest;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.home.dal.abtest.AbtestReportEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class ABTestReportBuilder implements ReportTaskBuilder, Initializable {
	@Inject
	protected ReportService m_reportService;

	@Inject
	private AbtestReportDao m_abtestReportDao;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private ABTestService m_abtestService;

	private Calendar m_calendar = Calendar.getInstance();

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		List<AbtestRun> runs = m_abtestService.getAbtestRunByStatus(AbtestStatus.RUNNING);

		for (AbtestRun run : runs) {
			Set<String> productLineSet = getProductLinesByRunID(run);

			buildHourlyTaskInternal(period, productLineSet);
		}

		return true;
	}

	private Date resetTime(String period, Date time) {
		m_calendar.setTime(time);
		m_calendar.set(Calendar.MINUTE, 0);
		m_calendar.set(Calendar.SECOND, 0);
		m_calendar.set(Calendar.MILLISECOND, 0);

		if (period.equals("day")) {
			m_calendar.set(Calendar.HOUR_OF_DAY, 0);
		}

		return m_calendar.getTime();
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

	@Override
	public void initialize() throws InitializationException {
		Date now = resetTime("hour", new Date());
		List<AbtestRun> runs = m_abtestService.getAbtestRunByStatus(AbtestStatus.RUNNING);

		for (AbtestRun run : runs) {
			Set<String> productLineSet = getProductLinesByRunID(run);
			Date period = getLatestPeriod(now, run.getId());

			m_calendar.setTime(period);

			while (!period.after(now)) {
				buildHourlyTaskInternal(period, productLineSet);

				m_calendar.add(Calendar.HOUR, 1);
				period = m_calendar.getTime();
			}
		}
	}

	private Date getLatestPeriod(Date now, int runId) {
		com.dianping.cat.home.dal.abtest.AbtestReport latestReport = null;
		Date period = null;

		try {
			latestReport = m_abtestReportDao.findLatestReportByRunId(runId, AbtestReportEntity.READSET_FULL);
		} catch (Exception e) {
			// ignore it
		}

		if (latestReport == null) {
			m_calendar.setTime(now);
			m_calendar.add(Calendar.DAY_OF_MONTH, -14);

			period = m_calendar.getTime();
		} else {
			period = latestReport.getPeriod();
		}

		return period;
	}

	private Set<String> getProductLinesByRunID(AbtestRun run) {
		String[] domains = run.getDomains().split(",");
		Set<String> productLineSet = new HashSet<String>();

		for (String domain : domains) {
			String productLine = m_productLineConfigManager.queryProductLineByDomain(domain);

			if (!productLine.equals("Default")) {
				productLineSet.add(productLine);
			}
		}
		return productLineSet;
	}

	private void buildHourlyTaskInternal(Date period, Set<String> productLineSet) {
		MetricReportForABTestVisitor visitor = new MetricReportForABTestVisitor();

		for (String productLine : productLineSet) {
			MetricReport metricReport = m_reportService.queryMetricReport(productLine, period, new Date(period.getTime()
			      + TimeUtil.ONE_HOUR));

			metricReport.accept(visitor);
		}

		Map<Integer, AbtestReport> result = visitor.getReportMap();

		for (AbtestReport report : result.values()) {
			if (report.getRunId() != -1) {
				com.dianping.cat.home.dal.abtest.AbtestReport _report = new com.dianping.cat.home.dal.abtest.AbtestReport();

				_report.setRunId(report.getRunId());
				_report.setPeriod(period);
				_report.setContent(report.toString());

				try {
					m_abtestReportDao.insert(_report);
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		}
	}
}
