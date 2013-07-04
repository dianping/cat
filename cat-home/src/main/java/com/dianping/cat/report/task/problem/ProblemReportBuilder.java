package com.dianping.cat.report.task.problem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class ProblemReportBuilder implements ReportBuilder {
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

	private void buildDailyGraph(ProblemReport report) {
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
	public boolean buildDailyReport(String name, String domain, Date period) {
		try {
			ProblemReport problemReport = queryDailyReportData(name, domain, period);
			buildDailyGraph(problemReport);

			String content = problemReport.toString();
			DailyReport report = new DailyReport();

			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			return m_reportService.insertDailyReport(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String name, String domain, Date period) {
		try {
			List<Graph> graphs = queryHourlyReport(name, domain, period);
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

	private ProblemReport buildMergedDailyReport(String domain, Date start, Date end) {
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

	@Override
	public boolean buildMonthReport(String name, String domain, Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);

		Date start = period;
		Date end = cal.getTime();

		ProblemReport problemReport = buildMergedDailyReport(domain, start, end);
		MonthlyReport report = new MonthlyReport();

		report.setContent(problemReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertMonthlyReport(report);
	}

	@Override
	public boolean buildWeeklyReport(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		ProblemReport problemReport = buildMergedDailyReport(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = problemReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertWeeklyReport(report);
	}

	private ProblemReport queryDailyReportData(String name, String domain, Date period) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(period);
		Set<String> domainSet = m_reportService.queryAllDomainNames(period, endDate, "problem");
		List<ProblemReport> reports = new ArrayList<ProblemReport>();
		long startTime = period.getTime();
		long endTime = endDate.getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			ProblemReport report = m_reportService.queryProblemReport(domain, new Date(startTime), new Date(startTime
			      + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		return m_problemMerger.mergeForDaily(domain, reports, domainSet);
	}

	private List<Graph> queryHourlyReport(String name, String domain, Date period) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<ProblemReport> reports = new ArrayList<ProblemReport>();
		long startTime = period.getTime();
		long endTime = TaskHelper.tomorrowZero(period).getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			ProblemReport report = m_reportService.queryProblemReport(domain, new Date(startTime), new Date(startTime
			      + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		ProblemReport problemReport = m_problemMerger.mergeForGraph(domain, reports);

		graphs = m_problemGraphCreator.splitReportToGraphs(period, domain, name, problemReport);
		return graphs;
	}
}
