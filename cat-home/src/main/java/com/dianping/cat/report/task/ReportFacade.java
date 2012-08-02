package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.site.lookup.annotation.Inject;

public class ReportFacade implements LogEnabled, Initializable {

	private static final int TYPE_DAILY = 1;

	private static final int TYPE_HOUR = 0;

	private Map<String, ReportBuilder> m_reportBuilders = new HashMap<String, ReportBuilder>();

	private Logger m_logger;

	@Inject
	private TransactionReportBuilder m_tansactionBuilder;

	@Inject
	private ProblemReportBuilder m_problemBuilder;

	@Inject
	private EventReportBuilder m_eventBuilder;

	@Inject
	private HeartbeatReportBuilder m_heartbeatBuilder;

	@Inject
	private TaskDao taskDao;

	public void addNewReportBuild(ReportBuilder newReportBuilder, String name) {
		this.m_reportBuilders.put(name, newReportBuilder);
	}

	private ReportBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	public boolean builderReport(Task task) {
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
				return reportBuilder.buildDailyReport(reportName, reportDomain, reportPeriod);
			} else if (task_type == TYPE_HOUR) {
				return reportBuilder.buildHourReport(reportName, reportDomain, reportPeriod);
			}
		}
		return false;
	}

	public boolean redoTask(int taskID) {
		boolean update = false;
		try {
			Task task = taskDao.findByPK(taskID, TaskEntity.READSET_FULL);
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
				taskDao.updateFailureToDone(task, TaskEntity.UPDATESET_FULL);
			}
			return update;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBuilders.put("problem", m_problemBuilder);
		m_reportBuilders.put("event", m_eventBuilder);
		m_reportBuilders.put("heartbeat", m_heartbeatBuilder);
		m_reportBuilders.put("transaction", m_tansactionBuilder);
	}
}
