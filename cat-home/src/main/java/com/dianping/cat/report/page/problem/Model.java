package com.dianping.cat.report.page.problem;

import java.util.Collection;
import java.util.Collections;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private ProblemReport m_report;

	private int m_lastMinute;

	private String m_ipAddress;

	private int m_hour;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return null;
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			return Collections.emptySet();
		} else {
			return m_report.getAllDomains().getDomains();
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
}
