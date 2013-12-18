package com.dianping.cat.report.task.problem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class ProblemReportBuilder implements ReportTaskBuilder {
	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected ReportService m_reportService;

	@Inject
	private ProblemGraphCreator m_problemGraphCreator;

	@Inject
	private ProblemMerger m_problemMerger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			ProblemReport problemReport = queryHourlyReportsByDuration(name, domain, period,
			      TaskHelper.tomorrowZero(period));
			buildProblemDailyGraph(problemReport);

			DailyReport report = new DailyReport();

			report.setContent("");
			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(problemReport);

			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	private List<Graph> buildHourlyGraphs(String name, String domain, Date period) throws DalException {
		long startTime = period.getTime();
		ProblemReport report = m_reportService.queryProblemReport(domain, new Date(startTime), new Date(startTime
		      + TimeUtil.ONE_HOUR));

		return m_problemGraphCreator.splitReportToGraphs(period, domain, ProblemAnalyzer.ID, report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			List<Graph> graphs = buildHourlyGraphs(name, domain, period);
			
			if (graphs != null) {
				for (Graph graph : graphs) {
					this.m_graphDao.insert(graph); // use mysql unique index and
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		ProblemReport problemReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(problemReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	private void buildProblemDailyGraph(ProblemReport report) {
		try {
			ProblemDailyGraphCreator creator = new ProblemDailyGraphCreator();
			creator.visitProblemReport(report);

			List<DailyGraph> graphs = creator.buildDailyGraph();

			for (DailyGraph temp : graphs) {
				m_dailyGraphDao.insert(temp);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		ProblemReport problemReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(problemReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private ProblemReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				ProblemReport reportModel = m_reportService.queryProblemReport(domain, new Date(startTime), new Date(
				      startTime + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		ProblemReport problemReport = merger.getProblemReport();

		problemReport.setStartTime(start);
		problemReport.setEndTime(end);
		return problemReport;
	}

	private ProblemReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end)
	      throws DalException {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, ProblemAnalyzer.ID);
		List<ProblemReport> reports = new ArrayList<ProblemReport>();
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			ProblemReport report = m_reportService.queryProblemReport(domain, new Date(startTime), new Date(startTime
			      + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		return m_problemMerger.mergeForDaily(domain, reports, domainSet);
	}
}
