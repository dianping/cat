package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.task.AbstractReportBuilder;
import com.dianping.cat.report.task.ReportBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.site.dal.jdbc.DalException;

public class TransactionReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	private TransactionGraphCreator m_transactionGraphCreator = new TransactionGraphCreator();

	private TransactionMerger m_transactionMerger = new TransactionMerger();

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = new HashSet<String>();
		getDomainSet(domainSet, reportPeriod, endDate);
		String content = null;
		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain,
			      reportName, ReportEntity.READSET_FULL);
			content = m_transactionMerger.mergeForDaily(reportDomain, reports, domainSet).toString();
			Dailyreport report = m_dailyReportDao.createLocal();
			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(reportDomain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(reportName);
			report.setPeriod(reportPeriod);
			report.setType(1);
			clearDailyGraph(report);
			m_dailyReportDao.insert(report);
			return true;
		} catch (DalException e) {
			Cat.getProducer().logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		List<Graph> graphs = new ArrayList<Graph>();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
			      ReportEntity.READSET_FULL);
			TransactionReport transactionReport = m_transactionMerger.mergeForGraph(reportDomain, reports);
			graphs = m_transactionGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName,
			      transactionReport);
			if (graphs != null) {
				clearGraphs(graphs);
				for (Graph graph : graphs) {
					m_graphDao.insert(graph); // use mysql unique index and insert
				}
			}
		} catch (DalException e) {
			Cat.getProducer().logError(e);
			return false;
		}
		return true;
	}

}
