package com.dianping.cat.report.task.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dainping.cat.consumer.dal.report.TaskEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.database.DatabaseReportBuilder;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.health.HealthReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.sql.SqlReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import org.unidal.lookup.annotation.Inject;

public class ReportFacade implements LogEnabled, Initializable {

	public static final int TYPE_HOUR = 0;

	public static final int TYPE_DAILY = 1;

	public static final int TYPE_WEEK = 2;

	public static final int TYPE_MONTH = 3;

	public static final int TYPE_DAILY_GRAPH = 4;

	@Inject
	private EventReportBuilder m_eventBuilder;

	@Inject
	private HeartbeatReportBuilder m_heartbeatBuilder;

	@Inject
	private ProblemReportBuilder m_problemBuilder;

	@Inject
	private TransactionReportBuilder m_tansactionBuilder;

	@Inject
	private MatrixReportBuilder m_matrixReportBuilder;

	@Inject
	private CrossReportBuilder m_crossReportBuilder;

	@Inject
	private DatabaseReportBuilder m_databaseReportBuilder;

	@Inject
	private SqlReportBuilder m_sqlReportBuilder;

	@Inject
	private HealthReportBuilder m_healthReportBuilder;

	@Inject
	private TaskDao m_taskDao;

	private Logger m_logger;

	private Map<String, ReportBuilder> m_reportBuilders = new HashMap<String, ReportBuilder>();

	public void addNewReportBuild(ReportBuilder newReportBuilder, String name) {
		this.m_reportBuilders.put(name, newReportBuilder);
	}

	public boolean builderReport(Task task) {
		int type = task.getTaskType();
		String reportName = task.getReportName();
		String reportDomain = task.getReportDomain();
		Date reportPeriod = task.getReportPeriod();
		ReportBuilder reportBuilder = getReportBuilder(reportName);

		if (reportBuilder == null) {
			m_logger.info("no report builder for type:" + " " + reportName);
			return false;
		} else {
			if (type == TYPE_DAILY) {
				return reportBuilder.buildDailyReport(reportName, reportDomain, reportPeriod);
			} else if (type == TYPE_HOUR) {
				return reportBuilder.buildHourReport(reportName, reportDomain, reportPeriod);
			} else if (type == TYPE_WEEK) {
				return reportBuilder.buildWeeklyReport(reportName, reportDomain, reportPeriod);
			} else if (type == TYPE_MONTH) {
				return reportBuilder.buildMonthReport(reportName, reportDomain, reportPeriod);
			}
		}
		return false;
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

	private ReportBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBuilders.put("problem", m_problemBuilder);
		m_reportBuilders.put("event", m_eventBuilder);
		m_reportBuilders.put("heartbeat", m_heartbeatBuilder);
		m_reportBuilders.put("transaction", m_tansactionBuilder);
		m_reportBuilders.put("matrix", m_matrixReportBuilder);
		m_reportBuilders.put("cross", m_crossReportBuilder);
		m_reportBuilders.put("database", m_databaseReportBuilder);
		m_reportBuilders.put("sql", m_sqlReportBuilder);
		m_reportBuilders.put("health", m_healthReportBuilder);
	}

	public boolean redoTask(int taskID) {
		boolean update = false;
		try {
			Task task = m_taskDao.findByPK(taskID, TaskEntity.READSET_FULL);
			int task_type = task.getTaskType();
			String reportName = task.getReportName();
			String reportDomain = task.getReportDomain();
			Date reportPeriod = task.getReportPeriod();
			ReportBuilder reportBuilder = getReportBuilder(reportName);
			if (reportBuilder == null) {
				m_logger.info("no report builder for type:" + " " + reportName);
				return false;
			} else {
				if (task_type == TYPE_DAILY) {
					update = reportBuilder.redoDailyReport(reportName, reportDomain, reportPeriod);
				} else if (task_type == TYPE_HOUR) {
					update = reportBuilder.redoHourReport(reportName, reportDomain, reportPeriod);
				}
			}
			if (update) {
				m_taskDao.updateFailureToDone(task, TaskEntity.UPDATESET_FULL);
			}
			return update;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}
}
