package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.task.TaskManager;

public class ReportFacade extends ContainerHolder implements LogEnabled, Initializable {

	private Logger m_logger;

	private Map<String, TaskBuilder> m_reportBuilders = new HashMap<String, TaskBuilder>();

	public boolean builderReport(Task task) {
		try {
			if (task == null) {
				return false;
			}
			int type = task.getTaskType();
			String reportName = task.getReportName();
			String reportDomain = task.getReportDomain();
			Date reportPeriod = task.getReportPeriod();
			TaskBuilder reportBuilder = getReportBuilder(reportName);

			if (reportBuilder == null) {
				Cat.logError(new RuntimeException("no report builder for type:" + " " + reportName));
				return false;
			} else {
				boolean result = false;

				if (type == TaskManager.REPORT_HOUR) {
					result = reportBuilder.buildHourlyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_DAILY) {
					result = reportBuilder.buildDailyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_WEEK) {
					result = reportBuilder.buildWeeklyTask(reportName, reportDomain, reportPeriod);
				} else if (type == TaskManager.REPORT_MONTH) {
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

	private TaskBuilder getReportBuilder(String reportName) {
		return m_reportBuilders.get(reportName);
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBuilders = lookupMap(TaskBuilder.class);
	}

}
