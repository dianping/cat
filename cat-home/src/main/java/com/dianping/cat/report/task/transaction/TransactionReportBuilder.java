package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class TransactionReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	@Inject
	private TransactionGraphCreator m_transactionGraphCreator;

	@Inject
	private TransactionMerger m_transactionMerger;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getdailyReport(reportName, reportDomain, reportPeriod);
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
			List<Graph> graphs = getHourReport(reportName, reportDomain, reportPeriod);
			if (graphs != null) {
				for (Graph graph : graphs) {
					m_graphDao.insert(graph); // use mysql unique index and insert
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private Dailyreport getdailyReport(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = getDomains(reportPeriod, endDate);
		
		List<Report> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		String content = m_transactionMerger.mergeForDaily(reportDomain, reports, domainSet).toString();

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

	private List<Graph> getHourReport(String reportName, String reportDomain, Date reportPeriod) throws DalException {
		List<Graph> graphs = new ArrayList<Graph>();
		List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
		      ReportEntity.READSET_FULL);
		TransactionReport transactionReport = m_transactionMerger.mergeForGraph(reportDomain, reports);
		graphs = m_transactionGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, transactionReport);
		return graphs;
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getdailyReport(reportName, reportDomain, reportPeriod);
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
			List<Graph> graphs = getHourReport(reportName, reportDomain, reportPeriod);
			if (graphs != null) {
				clearHourlyGraphs(graphs);
				for (Graph graph : graphs) {
					m_graphDao.insert(graph); // use mysql unique index and insert
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}
}
