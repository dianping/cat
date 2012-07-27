package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.task.AbstractReportBuilder;
import com.dianping.cat.report.task.ReportBuilder;
import com.site.dal.jdbc.DalException;

public class HeartbeatReportBuilder extends AbstractReportBuilder implements ReportBuilder{
	
	private HeartbeatMerger m_heartbeatMerger = new HeartbeatMerger();

	private HeartbeatGraphCreator m_heartbeatGraphCreator = new HeartbeatGraphCreator();

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException( "no daily report builder for heartbeat!" );
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		List<Graph> graphs = new ArrayList<Graph>();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
			      ReportEntity.READSET_FULL);
			HeartbeatReport transactionReport = m_heartbeatMerger.mergeForGraph(reportDomain, reports);
			graphs = m_heartbeatGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, transactionReport);
			if (graphs != null) {
				clearGraphs(graphs);
				for (Graph graph : graphs) {
					this.m_graphDao.insert(graph); // use mysql unique index and insert
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}
}
