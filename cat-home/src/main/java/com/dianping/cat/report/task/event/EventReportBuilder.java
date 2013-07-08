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
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class EventReportBuilder implements ReportBuilder {

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected ReportService m_reportService;

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
	public boolean buildDailyReport(String name, String domain, Date period) {
		try {
			EventReport eventReport = queryDailyReportData(name, domain, period);

			try {
				buildDailyEventGraph(eventReport);
			} catch (Exception e) {
				Cat.logError(e);
			}

			String content = eventReport.toString();
			DailyReport report = new DailyReport();

			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);

			return m_reportService.insertDailyReport(report);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String name, String domain, Date period) {
		try {
			List<Graph> graphs = queryHourlyReportData(name, domain, period);
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
				EventReport reportModel = m_reportService.queryEventReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
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
	public boolean buildMonthReport(String name, String domain, Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);

		Date start = period;
		Date end = cal.getTime();

		EventReport eventReport = buildMergedDailyReport(domain, start, end);
		MonthlyReport report = new MonthlyReport();

		report.setContent(eventReport.toString());
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

		EventReport eventReport = buildMergedDailyReport(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = eventReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertWeeklyReport(report);
	}

	private EventReport queryDailyReportData(String name, String domain, Date period) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(period);
		Set<String> domainSet = m_reportService.queryAllDomainNames(period, endDate, "event");
		List<EventReport> reports = new ArrayList<EventReport>();
		long startTime = period.getTime();
		long endTime = endDate.getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			EventReport report = m_reportService.queryEventReport(domain, new Date(startTime), new Date(startTime
			      + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		return m_eventMerger.mergeForDaily(domain, reports, domainSet);
	}

	private List<Graph> queryHourlyReportData(String name, String domain, Date period) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<EventReport> reports = new ArrayList<EventReport>();
		long startTime = period.getTime();
		long endTime = TaskHelper.tomorrowZero(period).getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			EventReport report = m_reportService.queryEventReport(domain, new Date(startTime), new Date(startTime
			      + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		EventReport eventReport = m_eventMerger.mergeForGraph(domain, reports);

		graphs = m_eventGraphCreator.splitReportToGraphs(period, domain, name, eventReport);
		return graphs;
	}

}
