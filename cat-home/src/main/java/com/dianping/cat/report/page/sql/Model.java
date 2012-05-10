package com.dianping.cat.report.page.sql;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private SqlReport m_report;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	private String m_statement;

	private String m_mobileResponse;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public SqlReport getReport() {
		return m_report;
	}

	public void setReport(SqlReport report) {
		this.m_report = report;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomains());
		}
	}

	public String getGraph1() {
		return m_graph1;
	}

	public void setGraph1(String graph1) {
		m_graph1 = graph1;
	}

	public String getGraph2() {
		return m_graph2;
	}

	public void setGraph2(String graph2) {
		m_graph2 = graph2;
	}

	public String getGraph3() {
		return m_graph3;
	}

	public void setGraph3(String graph3) {
		m_graph3 = graph3;
	}

	public String getGraph4() {
		return m_graph4;
	}

	public void setGraph4(String graph4) {
		m_graph4 = graph4;
	}

	public String getStatement() {
		return m_statement;
	}

	public void setStatement(String statement) {
		m_statement = statement;
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}
}
