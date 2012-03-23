package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.AllDomains;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private ProblemReport m_report;

	private int m_lastMinute; // last minute of current hour

	private String m_ipAddress;

	private int m_hour;

	private String m_threadId;

	private int m_currentMinute; // for navigation

	private Map<String, ProblemStatistics> m_statistics = new HashMap<String, ProblemStatistics>();

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public int getMinuteLast() {
		if (m_currentMinute == 0) {
			return 0;
		}
		return m_currentMinute - 1;
	}

	public int getMinuteNext() {
		if (m_currentMinute == 59) {
			return 59;
		}
		return m_currentMinute + 1;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
<<<<<<< HEAD
			return getDefaultDomain();
=======
			return getDisplayDomain();
>>>>>>> 8fba9da1445e5bf08a418057a70f787f909d543f
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			AllDomains allDomains = m_report.getAllDomains();

			if (allDomains == null) {
				return Collections.emptySet();
			} else {
				return allDomains.getDomains();
			}
		}
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public int getLastMinute() {
		return m_lastMinute;
	}

	public int getHour() {
		return m_hour;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	public ProblemReport getReport() {
		return m_report;
	}

	public void setLastMinute(int lastMinute) {
		m_lastMinute = lastMinute;
	}

	public void setReport(ProblemReport report) {
		m_report = report;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public Map<String, ProblemStatistics> getStatistics() {
		return m_statistics;
	}

	public void setStatistics(Map<String, ProblemStatistics> statistics) {
		this.m_statistics = statistics;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public int getCurrentMinute() {
		return m_currentMinute;
	}

	public void setCurrentMinute(int currentMinute) {
		m_currentMinute = currentMinute;
	}
}
