package com.dianping.cat.report.task.cached;

import java.util.Date;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;

public class CachedReportTask implements Task {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private ServerConfigManager m_configManger;

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

	private MonthlyReport buildMonthlyReport(String domain, Date period, String name) {
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return report;
	}

	private WeeklyReport buildWeeklyReport(String domain, Date period, String name) {
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return report;
	}

	@Override
	public String getName() {
		return "Cached-Report-Task";
	}

	private void reloadCurrentMonthly() {
		Date start = TimeHelper.getCurrentMonth();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_configManger.validateDomain(domain)) {
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
		String domain = Constants.CAT;

		StateReport stateReport = m_reportService.queryStateReport(domain, start, end);
		m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, StateAnalyzer.ID),
		      com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder.build(stateReport));
	}

	private void reloadCurrentWeekly() {
		Date start = TimeHelper.getCurrentWeek();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_configManger.validateDomain(domain)) {
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
		String domain = Constants.CAT;

		StateReport stateReport = m_reportService.queryStateReport(domain, start, end);
		m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, StateAnalyzer.ID),
		      com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder.build(stateReport));
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
