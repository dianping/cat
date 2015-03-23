package com.dianping.cat.report.page.event.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportCountFilter;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class EventReportBuilder implements TaskBuilder {

	public static final String ID = EventAnalyzer.ID;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected EventReportService m_reportService;

	@Inject
	private EventGraphCreator m_eventGraphCreator;

	@Inject
	private EventMerger m_eventMerger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			EventReport eventReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));

			buildEventDailyGraph(eventReport);

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void buildEventDailyGraph(EventReport report) {
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

	private List<Graph> buildHourlyGraphs(String name, String domain, Date period) throws DalException {
		long startTime = period.getTime();
		EventReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
		      + TimeHelper.ONE_HOUR));

		return m_eventGraphCreator.splitReportToGraphs(period, domain, EventAnalyzer.ID, report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			List<Graph> graphs = buildHourlyGraphs(name, domain, period);
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

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}

		EventReport eventReport = queryDailyReportsByDuration(domain, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		EventReport eventReport = queryDailyReportsByDuration(domain, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private EventReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;
		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(domain)).setDuration(duration);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				EventReport reportModel = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);

		new EventReportCountFilter().visitEventReport(eventReport);
		return eventReport;
	}

	private EventReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end)
	      throws DalException {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, EventAnalyzer.ID);
		List<EventReport> reports = new ArrayList<EventReport>();
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			EventReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
			      + TimeHelper.ONE_HOUR));

			reports.add(report);
		}
		return m_eventMerger.mergeForDaily(domain, reports, domainSet, duration);
	}

}
