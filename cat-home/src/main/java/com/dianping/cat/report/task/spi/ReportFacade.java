package com.dianping.cat.report.task.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.dal.Task;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.sql.SqlReportBuilder;
import com.dianping.cat.report.task.state.StateReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;

public class ReportFacade implements LogEnabled, Initializable {

	public static final int TYPE_HOUR = 0;

	public static final int TYPE_DAILY = 1;

	public static final int TYPE_WEEK = 2;

	public static final int TYPE_MONTH = 3;

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
	private SqlReportBuilder m_sqlReportBuilder;

	@Inject
	private StateReportBuilder m_stateReportBuilder;
	
	@Inject
	private DependencyReportBuilder m_dependendcyReportBuilder;

	private Logger m_logger;

	private Map<String, ReportBuilder> m_reportBuilders = new HashMap<String, ReportBuilder>();

	public void addNewReportBuild(ReportBuilder newReportBuilder, String name) {
		m_reportBuilders.put(name, newReportBuilder);
	}

	public boolean builderReport(Task task) {
		try {
			if (task == null) {
				return false;
			}
			int type = task.getTaskType();
			String reportName = task.getReportName();
			String reportDomain = task.getReportDomain();
			Date reportPeriod = task.getReportPeriod();
			ReportBuilder reportBuilder = getReportBuilder(reportName);

			if (reportBuilder == null) {
				m_logger.info("no report builder for type:" + " " + reportName);
				return false;
			} else {
				boolean result = false;

				if (type == TYPE_HOUR) {
					result = reportBuilder.buildHourReport(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_DAILY) {
					result = reportBuilder.buildDailyReport(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_WEEK) {
					result = reportBuilder.buildWeeklyReport(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_MONTH) {
					result = reportBuilder.buildMonthReport(reportName, reportDomain, reportPeriod);
				}
				if (result) {
					return result;
				} else {
					m_logger.error(task.toString());
				}
			}
		} catch (Exception e) {
			m_logger.error("Error when building report," + e.getMessage(), e);
			Cat.logError(e);
			return false;
		}
		return false;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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
		m_reportBuilders.put("sql", m_sqlReportBuilder);
		m_reportBuilders.put("state", m_stateReportBuilder);
		m_reportBuilders.put("dependency", m_dependendcyReportBuilder);
	}

}
