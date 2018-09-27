package com.dianping.cat.report.page.server.display;

import com.dianping.cat.server.QueryParameter;

public class LinechartParameter {

	private String m_id;

	private String m_title;

	private QueryParameter m_parameter;

	public LinechartParameter(String id, String title, QueryParameter parameter) {
		m_id = id;
		m_title = title;
		m_parameter = parameter;
	}

	public String getId() {
		return m_id;
	}

	public QueryParameter getParameter() {
		return m_parameter;
	}

	public String getTitle() {
		return m_title;
	}
}
