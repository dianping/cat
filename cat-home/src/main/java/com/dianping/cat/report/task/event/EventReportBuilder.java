package com.dianping.cat.report.task.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class EventReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	@Inject
	private EventGraphCreator m_eventGraphCreator;

	@Inject
	private EventMerger m_eventMerger;

	private void buildDailyEventGraph(EventReport report) {
		DailyEventGraphCreator creator = new DailyEventGraphCreator();
		List<DailyGraph> graphs = creator.buildDailygraph(report);

		for (DailyGraph graph : graphs) {
			try {
				m_dailyGraphDao.insert(graph);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			EventReport eventReport = getDailyReportData(reportName, reportDomain, reportPeriod);

			try {
				buildDailyEventGraph(eventReport);
			} catch (Exception e) {
				Cat.logError(e);
			}

			String content = eventReport.toString();
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
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			List<Graph> graphs = getHourReportData(reportName, reportDomain, reportPeriod);
			if (graphs != null) {
				for (Graph graph : graphs) {
					this.m_graphDao.insert(graph);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private EventReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				DailyReport dailyreport = m_dailyReportDao.findByDomainNamePeriod(domain, "event",
				      new Date(startTime), DailyReportEntity.READSET_FULL);
				String xml = dailyreport.getContent();

				EventReport reportModel = DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();
		eventReport.setStartTime(start);
		eventReport.setEndTime(end);
		return eventReport;
	}

	@Override
	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.MONTH, 1);

		Date start = reportPeriod;
		Date end = cal.getTime();

		EventReport eventReport = buildMergedDailyReport(reportDomain, start, end);
		MonthlyReport report = m_monthlyReportDao.createLocal();

		report.setContent(eventReport.toString());
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

		EventReport eventReport = buildMergedDailyReport(reportDomain, start, end);
		WeeklyReport report = m_weeklyReportDao.createLocal();
		String content = eventReport.toString();

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

	private EventReport getDailyReportData(String reportName, String reportDomain, Date reportPeriod)
	      throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = getDomainsFromHourlyReport(reportPeriod, endDate);
		List<HourlyReport> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
		      HourlyReportEntity.READSET_FULL);

		return m_eventMerger.mergeForDaily(reportDomain, reports, domainSet);

	}

	private List<Graph> getHourReportData(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<HourlyReport> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
		      HourlyReportEntity.READSET_FULL);
		EventReport eventReport = m_eventMerger.mergeForGraph(reportDomain, reports);
		graphs = m_eventGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, eventReport);
		return graphs;
	}
}
