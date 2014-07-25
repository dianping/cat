package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.TransactionReportUrlFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class TransactionReportBuilder implements ReportTaskBuilder, LogEnabled {
	
	public static final String ID = TransactionAnalyzer.ID;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected ReportServiceManager m_reportService;

	@Inject
	private TransactionGraphCreator m_transactionGraphCreator;

	@Inject
	private TransactionMerger m_transactionMerger;

	private Logger m_logger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			Date end = TaskHelper.tomorrowZero(period);
			TransactionReport transactionReport = queryHourlyReportsByDuration(name, domain, period, end);

			buildDailyTransactionGraph(transactionReport);

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setContent("");
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			Cat.logError(e);
			return false;
		}
	}

	private void buildDailyTransactionGraph(TransactionReport report) {
		DailyTransactionGraphCreator creator = new DailyTransactionGraphCreator();
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
		TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(startTime), new Date(startTime
		      + TimeUtil.ONE_HOUR));
		
		return m_transactionGraphCreator.splitReportToGraphs(period, domain, TransactionAnalyzer.ID, report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			List<Graph> graphs = buildHourlyGraphs(name, domain, period);
			if (graphs != null) {
				for (Graph graph : graphs) {
					m_graphDao.insert(graph);
				}
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period,
		      TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(transactionReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private TransactionReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				TransactionReport reportModel = m_reportService.queryTransactionReport(domain, new Date(startTime),
				      new Date(startTime + TimeUtil.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);
		new TransactionReportUrlFilter().visitTransactionReport(transactionReport);
		return transactionReport;
	}

	private TransactionReport queryHourlyReportsByDuration(String name, String domain, Date start, Date endDate)
	      throws DalException {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, endDate, TransactionAnalyzer.ID);
		List<TransactionReport> reports = new ArrayList<TransactionReport>();
		long startTime = start.getTime();
		long endTime = endDate.getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(startTime), new Date(
			      startTime + TimeUtil.ONE_HOUR));

			reports.add(report);
		}
		return m_transactionMerger.mergeForDaily(domain, reports, domainSet);
	}
}
