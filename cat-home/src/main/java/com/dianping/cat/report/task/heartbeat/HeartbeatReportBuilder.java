package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class HeartbeatReportBuilder implements ReportBuilder {

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected ReportService m_reportService;

	@Inject
	private HeartbeatGraphCreator m_heartbeatGraphCreator;

	@Override
	public boolean buildDailyReport(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no daily report builder for heartbeat!");
	}

	@Override
	public boolean buildHourReport(String name, String domain, Date period) {
		try {
			List<Graph> graphs = getHourReportData(name, domain, period);
			if (graphs != null) {
				for (Graph graph : graphs) {
					m_graphDao.insert(graph); // use mysql unique index and
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private HeartbeatReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				HeartbeatReport reportModel = m_reportService.queryHeartbeatReport(domain, new Date(startTime), new Date(
				      startTime + TimeUtil.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();
		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(end);
		return heartbeatReport;
	}

	@Override
	public boolean buildMonthReport(String name, String domain, Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);

		Date start = period;
		Date end = cal.getTime();

		HeartbeatReport heartbeatReport = buildMergedDailyReport(domain, start, end);
		MonthlyReport report = new MonthlyReport();

		report.setContent(heartbeatReport.toString());
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

		HeartbeatReport heartbeatReport = buildMergedDailyReport(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = heartbeatReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertWeeklyReport(report);
	}

	private List<Graph> getHourReportData(String name, String domain, Date period) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		HeartbeatReport transactionReport = m_reportService.queryHeartbeatReport(domain, period,
		      new Date(period.getTime() + TimeUtil.ONE_HOUR));
		graphs = m_heartbeatGraphCreator.splitReportToGraphs(period, domain, name, transactionReport);
		return graphs;
	}
}
