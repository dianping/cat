package com.dianping.cat.report.page.logview;

import java.util.Collection;
import java.util.Collections;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private String m_domain;

	private String m_mobileResponse;

	private String m_table;

	private String m_logviewPath;

	private MessageTree m_tree;

	public Model(Context ctx) {
		super(ctx);
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

	public String getLogviewPath() {
		return m_logviewPath;
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public String getTable() {
		return m_table;
	}

	public MessageTree getTree() {
		return m_tree;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setLogviewPath(String logviewPath) {
		m_logviewPath = logviewPath;
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}

	public void setTable(String table) {
		m_table = table;
	}

	public void setTree(MessageTree tree) {
		m_tree = tree;
	}

}
