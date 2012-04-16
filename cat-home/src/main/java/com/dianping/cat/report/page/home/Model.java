package com.dianping.cat.report.page.home;

import java.util.Collection;
import java.util.Collections;

import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private String m_domain;

	private String m_content;

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

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}
}
