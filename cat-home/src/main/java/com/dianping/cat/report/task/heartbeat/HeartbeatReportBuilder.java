package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.helper.TimeUtil;
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

	@Override
	public boolean buildMonthReport(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no month report builder for heartbeat!");
	}

	@Override
	public boolean buildWeeklyReport(String name, String domain, Date period) {
		throw new UnsupportedOperationException("no weekly report builder for heartbeat!");
	}

	private List<Graph> getHourReportData(String name, String domain, Date period) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		HeartbeatReport transactionReport = m_reportService.queryHeartbeatReport(domain, period,
		      new Date(period.getTime() + TimeUtil.ONE_HOUR));
		graphs = m_heartbeatGraphCreator.splitReportToGraphs(period, domain, name, transactionReport);
		return graphs;
	}
}
