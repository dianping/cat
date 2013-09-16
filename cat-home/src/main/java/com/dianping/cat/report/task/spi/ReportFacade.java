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
import com.dianping.cat.Constants;
import com.dianping.cat.ReportType;
import com.dianping.cat.consumer.advanced.MetricAnalyzer;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.report.task.abtest.ABTestReportBuilder;
import com.dianping.cat.report.task.bug.BugReportBuilder;
import com.dianping.cat.report.task.cross.CrossReportBuilder;
import com.dianping.cat.report.task.dependency.DependencyReportBuilder;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.task.matrix.MatrixReportBuilder;
import com.dianping.cat.report.task.metric.MetricBaselineReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.service.ServiceReportBuilder;
import com.dianping.cat.report.task.sql.SqlReportBuilder;
import com.dianping.cat.report.task.state.StateReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.dianping.cat.report.task.utilization.UtilizationReportBuilder;

public class ReportFacade implements LogEnabled, Initializable {

	public static final int TYPE_HOUR = ReportType.HOUR;

	public static final int TYPE_DAILY = ReportType.DAILY;

	public static final int TYPE_WEEK = ReportType.WEEK;

	public static final int TYPE_MONTH = ReportType.MONTH;

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
	private BugReportBuilder m_bugReportBuilder;

	@Inject
	private ServiceReportBuilder m_serviceReportBuilder;

	@Inject
	private DependencyReportBuilder m_dependendcyReportBuilder;

	@Inject
	private MetricBaselineReportBuilder m_metricBaselineReportBuilder;
	
	@Inject
	private ABTestReportBuilder m_abtestReportBuilder;

	@Inject
	private HeavyReportBuilder m_heavyReportBuilder;

	@Inject
	private UtilizationReportBuilder m_utilizationReportBuilder;

	private Logger m_logger;

	private Map<String, ReportTaskBuilder> m_reportBuilders = new HashMap<String, ReportTaskBuilder>();

	public void addNewReportBuild(ReportTaskBuilder newReportBuilder, String name) {
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
			ReportTaskBuilder reportBuilder = getReportBuilder(reportName);

			if (reportBuilder == null) {
				m_logger.info("no report builder for type:" + " " + reportName);
				return false;
			} else {
				boolean result = false;

				if (type == TYPE_HOUR) {
					result = reportBuilder.buildHourlyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_DAILY) {
					result = reportBuilder.buildDailyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_WEEK) {
					result = reportBuilder.buildWeeklyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TYPE_MONTH) {
					result = reportBuilder.buildMonthlyTask(reportName, reportDomain, reportPeriod);
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

	private ReportTaskBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBuilders.put(ProblemAnalyzer.ID, m_problemBuilder);
		m_reportBuilders.put(EventAnalyzer.ID, m_eventBuilder);
		m_reportBuilders.put(HeartbeatAnalyzer.ID, m_heartbeatBuilder);
		m_reportBuilders.put(TransactionAnalyzer.ID, m_tansactionBuilder);
		m_reportBuilders.put(MatrixAnalyzer.ID, m_matrixReportBuilder);
		m_reportBuilders.put(CrossAnalyzer.ID, m_crossReportBuilder);
		m_reportBuilders.put(SqlAnalyzer.ID, m_sqlReportBuilder);
		m_reportBuilders.put(StateAnalyzer.ID, m_stateReportBuilder);
		m_reportBuilders.put(DependencyAnalyzer.ID, m_dependendcyReportBuilder);
		m_reportBuilders.put(MetricAnalyzer.ID, m_metricBaselineReportBuilder);
		
		m_reportBuilders.put(Constants.REPORT_BUG, m_bugReportBuilder);
		m_reportBuilders.put(Constants.REPORT_SERVICE, m_serviceReportBuilder);
		m_reportBuilders.put(Constants.REPORT_HEAVY, m_heavyReportBuilder);
		m_reportBuilders.put(Constants.REPORT_UTILIZATION, m_utilizationReportBuilder);
		m_reportBuilders.put(Constants.ABTEST, m_abtestReportBuilder);
	}

}
