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
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class ProblemReportBuilder extends AbstractReportBuilder implements ReportBuilder {

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
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			ProblemReport problemReport = getDailyReportData(reportName, reportDomain, reportPeriod);
			buildDailyGraph(problemReport);

			String content = problemReport.toString();
			DailyReport report = m_dailyReportDao.createLocal();

			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(reportDomain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(reportName);
			report.setPeriod(reportPeriod);
			report.setType(1);
			m_dailyReportDao.insert(report);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			List<Graph> graphs = getHourlyReport(reportName, reportDomain, reportPeriod);
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
				DailyReport dailyreport = m_dailyReportDao.findReportByDomainNamePeriod(domain, "problem",
						new Date(startTime), DailyReportEntity.READSET_FULL);
				String xml = dailyreport.getContent();

				ProblemReport reportModel = DefaultSaxParser.parse(xml);
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
	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.MONTH, 1);

		Date start = reportPeriod;
		Date end = cal.getTime();

		ProblemReport problemReport = buildMergedDailyReport(reportDomain, start, end);
		MonthlyReport report = m_monthlyReportDao.createLocal();

		report.setContent(problemReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_monthlyReportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod) {
		Date start = reportPeriod;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		ProblemReport problemReport = buildMergedDailyReport(reportDomain, start, end);
		WeeklyReport report = m_weeklyReportDao.createLocal();
		String content = problemReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_weeklyReportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private ProblemReport getDailyReportData(String reportName, String reportDomain, Date reportPeriod)
	      throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = getDomainsFromHourlyReport(reportPeriod, endDate);
		List<HourlyReport> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
		      HourlyReportEntity.READSET_FULL);

		return m_problemMerger.mergeForDaily(reportDomain, reports, domainSet);
	}

	private List<Graph> getHourlyReport(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<HourlyReport> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
		      HourlyReportEntity.READSET_FULL);
		ProblemReport problemReport = m_problemMerger.mergeForGraph(reportDomain, reports);
		
		graphs = m_problemGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, problemReport);
		return graphs;
	}
}
