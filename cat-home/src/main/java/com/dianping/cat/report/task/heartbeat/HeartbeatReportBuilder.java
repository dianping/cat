package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class HeartbeatReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	@Inject
	private HeartbeatGraphCreator m_heartbeatGraphCreator;

	@Inject
	private HeartbeatMerger m_heartbeatMerger;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no daily report builder for heartbeat!");
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			List<Graph> graphs = getHourReportData(reportName, reportDomain, reportPeriod);
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

	private List<Graph> getHourReportData(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		HeartbeatReport transactionReport = m_heartbeatMerger.mergeForGraph(reportDomain, reports);
		graphs = m_heartbeatGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, transactionReport);
		return graphs;
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no daily report builder for heartbeat!");
	}

	@Override
	public boolean redoHourReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			List<Graph> graphs = getHourReportData(reportName, reportDomain, reportPeriod);
			if (graphs != null) {
				clearHourlyGraphs(graphs);
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
}
