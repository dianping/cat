package com.dianping.cat.report.task.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.ReportType;
import com.dianping.cat.core.dal.Task;

public class ReportFacade extends ContainerHolder implements LogEnabled, Initializable {

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

				if (type == ReportType.HOUR) {
					result = reportBuilder.buildHourlyTask(reportName, reportDomain, reportPeriod);
				} else if (type == ReportType.DAILY) {
					result = reportBuilder.buildDailyTask(reportName, reportDomain, reportPeriod);
				} else if (type == ReportType.WEEK) {
					result = reportBuilder.buildWeeklyTask(reportName, reportDomain, reportPeriod);
				} else if (type == ReportType.MONTH) {
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
		m_reportBuilders = lookupMap(ReportTaskBuilder.class);
	}

}
