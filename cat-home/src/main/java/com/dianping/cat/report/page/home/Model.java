package com.dianping.cat.report.page.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private String m_content;

	private String m_domain;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getContent() {
		return m_content;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public Collection<String> getDomains() {
		return Collections.emptySet();
	}

	@Override
	public List<String> getIps() {
		return new ArrayList<String>();
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}
}
