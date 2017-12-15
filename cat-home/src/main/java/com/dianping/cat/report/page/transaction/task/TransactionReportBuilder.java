package com.dianping.cat.report.page.transaction.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportCountFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class TransactionReportBuilder implements TaskBuilder, LogEnabled {

	public static final String ID = TransactionAnalyzer.ID;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected TransactionReportService m_reportService;

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
		TransactionReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
		      + TimeHelper.ONE_HOUR));

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
		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}
		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period, end);
		MonthlyReport report = new MonthlyReport();

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
		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		TransactionReport transactionReport = queryDailyReportsByDuration(domain, period, end);
		WeeklyReport report = new WeeklyReport();

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
		double duration = (end.getTime() - start.getTime()) * 1.0 / TimeHelper.ONE_DAY;
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(domain))
		      .setDuration(duration);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				TransactionReport reportModel = m_reportService.queryReport(domain, new Date(startTime),
				      new Date(startTime + TimeHelper.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);
		new TransactionReportCountFilter().visitTransactionReport(transactionReport);
		return transactionReport;
	}

	private TransactionReport queryHourlyReportsByDuration(String name, String domain, Date start, Date endDate)
	      throws DalException {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, endDate, TransactionAnalyzer.ID);
		List<TransactionReport> reports = new ArrayList<TransactionReport>();
		long startTime = start.getTime();
		long endTime = endDate.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			TransactionReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(
			      startTime + TimeHelper.ONE_HOUR));

			reports.add(report);
		}
		return m_transactionMerger.mergeForDaily(domain, reports, domainSet, duration);
	}
}
