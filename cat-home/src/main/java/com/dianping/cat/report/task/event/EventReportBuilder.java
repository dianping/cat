package com.dianping.cat.report.task.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.task.AbstractReportBuilder;
import com.dianping.cat.report.task.ReportBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.site.dal.jdbc.DalException;

public class EventReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	private EventGraphCreator m_eventGraphCreator = new EventGraphCreator();

	private EventMerger m_eventMerger = new EventMerger();

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getDailyReportData(reportName, reportDomain, reportPeriod);
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

	private Dailyreport getDailyReportData(String reportName, String reportDomain, Date reportPeriod)
	      throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = new HashSet<String>();
		getDomainSet(domainSet, reportPeriod, endDate);
		String content = null;
		List<Report> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		content = m_eventMerger.mergeForDaily(reportDomain, reports, domainSet).toString();
		Dailyreport report = m_dailyReportDao.createLocal();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);
		return report;

	}

	private List<Graph> getHourReportData(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		EventReport transactionReport = m_eventMerger.mergeForGraph(reportDomain, reports);
		graphs = m_eventGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, transactionReport);
		return graphs;
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getDailyReportData(reportName, reportDomain, reportPeriod);
			clearDailyReport(report);
			m_dailyReportDao.insert(report);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean redoHourReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			List<Graph> graphs = getHourReportData(reportName, reportDomain, reportPeriod);
			if (graphs != null) {
				clearHourlyGraphs(graphs);
				for (Graph graph : graphs) {
					m_graphDao.insert(graph);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}
}
