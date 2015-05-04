package com.dianping.cat.report.task.cached;

import java.util.Date;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;

public class CachedReportTask implements Task {

	@Inject
	private TransactionReportService m_transactionReportService;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private TransactionReportBuilder m_transactionReportBuilder;

	@Inject
	private EventReportBuilder m_eventReportBuilder;

	@Inject
	private ProblemReportBuilder m_problemReportBuilder;

	@Inject
	private CrossReportBuilder m_crossReportBuilder;

	@Inject
	private MatrixReportBuilder m_matrixReportBuilder;

	@Override
	public String getName() {
		return "Cached-Report-Task";
	}

	private void reloadCurrentMonthly() {
		Date start = TimeHelper.getCurrentMonth();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_transactionReportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_serverFilterConfigManager.validateDomain(domain)) {
				Transaction t = Cat.newTransaction("ReloadTask", "Reload-Month-" + domain);

				m_transactionReportBuilder.buildMonthlyTask(TransactionAnalyzer.ID, domain, start);
				m_eventReportBuilder.buildMonthlyTask(EventAnalyzer.ID, domain, start);
				m_problemReportBuilder.buildMonthlyTask(ProblemAnalyzer.ID, domain, start);
				m_crossReportBuilder.buildMonthlyTask(CrossAnalyzer.ID, domain, start);
				m_matrixReportBuilder.buildMonthlyTask(MatrixAnalyzer.ID, domain, start);

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			}
		}
	}

	private void reloadCurrentWeekly() {
		Date start = TimeHelper.getCurrentWeek();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_transactionReportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_serverFilterConfigManager.validateDomain(domain)) {
				Transaction t = Cat.newTransaction("ReloadTask", "Reload-Week-" + domain);

				m_transactionReportBuilder.buildWeeklyTask(TransactionAnalyzer.ID, domain, start);
				m_eventReportBuilder.buildWeeklyTask(EventAnalyzer.ID, domain, start);
				m_problemReportBuilder.buildWeeklyTask(ProblemAnalyzer.ID, domain, start);
				m_crossReportBuilder.buildWeeklyTask(CrossAnalyzer.ID, domain, start);
				m_matrixReportBuilder.buildWeeklyTask(MatrixAnalyzer.ID, domain, start);

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			}
		}
	}

	@Override
	public void run() {
		reloadCurrentWeekly();
		reloadCurrentMonthly();
	}

	@Override
	public void shutdown() {
	}

}
