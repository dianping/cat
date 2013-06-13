package com.dianping.cat.report.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.advanced.MatrixReportFilter;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.TransactionReportUrlFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;

public class MonthReportCache implements Initializable {

	private long m_end;

	private Map<String, TransactionReport> m_transactionReports = new HashMap<String, TransactionReport>();

	private Map<String, EventReport> m_eventReports = new HashMap<String, EventReport>();

	private Map<String, ProblemReport> m_problemReports = new HashMap<String, ProblemReport>();

	private Map<String, MatrixReport> m_matrixReports = new HashMap<String, MatrixReport>();

	private Map<String, CrossReport> m_crossReports = new HashMap<String, CrossReport>();

	private Map<String, SqlReport> m_sqlReports = new HashMap<String, SqlReport>();

	private Map<String, DatabaseReport> m_databaseRepors = new HashMap<String, DatabaseReport>();

	private Map<String, HealthReport> m_healthReports = new HashMap<String, HealthReport>();

	private Map<String, StateReport> m_stateReports = new HashMap<String, StateReport>();

	@Inject
	private DailyReportService m_dailyReportService;

	@Inject
	private HourlyReportService m_hourReportService;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat").start(new Reload());
		}
	}

	public CrossReport queryCrossReport(String domain, Date start) {
		return m_crossReports.get(domain);
	}

	public DatabaseReport queryDatabaseReport(String database, Date start) {
		return m_databaseRepors.get(database);
	}

	public EventReport queryEventReport(String domain, Date start) {
		return m_eventReports.get(domain);
	}

	public HealthReport queryHealthReport(String domain, Date start) {
		return m_healthReports.get(domain);
	}

	public HeartbeatReport queryHeartbeatReport(String domain, Date start) {
		return null;
	}

	public MatrixReport queryMatrixReport(String domain, Date start) {
		return m_matrixReports.get(domain);
	}

	public ProblemReport queryProblemReport(String domain, Date start) {
		return m_problemReports.get(domain);
	}

	public SqlReport querySqlReport(String domain, Date start) {
		return m_sqlReports.get(domain);
	}

	public StateReport queryStateReport(String domain, Date start) {
		return m_stateReports.get(domain);
	}

	public TransactionReport queryTransactionReport(String domain, Date start) {
		return m_transactionReports.get(domain);
	}

	public class Reload implements Task {
		@Override
		public String getName() {
			return "Month-Report-Cache";
		}

		private void reload() {
			Date start = TimeUtil.getCurrentMonth();
			Date end = TimeUtil.getCurrentDay();
			Set<String> domains = m_hourReportService.queryAllDomainNames(start, end, "transaction");

			for (String domain : domains) {
				TransactionReport transactionReport = m_dailyReportService.queryTransactionReport(domain, start, end);
				new TransactionReportUrlFilter().visitTransactionReport(transactionReport);
				
				m_transactionReports.put(domain, transactionReport);
				m_eventReports.put(domain, m_dailyReportService.queryEventReport(domain, start, end));
				m_problemReports.put(domain, m_dailyReportService.queryProblemReport(domain, start, end));
				m_crossReports.put(domain, m_dailyReportService.queryCrossReport(domain, start, end));
				MatrixReport matrixReport = m_dailyReportService.queryMatrixReport(domain, start, end);
				
				m_matrixReports.put(domain, matrixReport);
				new MatrixReportFilter().visitMatrixReport(matrixReport);
				m_sqlReports.put(domain, m_dailyReportService.querySqlReport(domain, start, end));
				m_healthReports.put(domain, m_dailyReportService.queryHealthReport(domain, start, end));
			}

			Set<String> databases = m_hourReportService.queryAllDatabaseNames(start, end, "database");

			for (String database : databases) {
				m_databaseRepors.put(database, m_dailyReportService.queryDatabaseReport(database, start, end));
			}

			String cat = "Cat";

			m_stateReports.put(cat, m_dailyReportService.queryStateReport(cat, start, end));
			m_end = end.getTime();
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				Date date = TimeUtil.getCurrentDay();
				long time = date.getTime();

				if (time > m_end) {
					Transaction t = Cat.newTransaction("ReportReload", "Month");

					try {
						reload();
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					} finally {
						t.complete();
					}
				}
				try {
					Thread.sleep(60 * 60 * 1000);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
