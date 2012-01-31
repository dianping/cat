package com.dianping.cat.report.page.logview;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_table;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getTable() {
		return m_table;
	}

	public void setTable(String table) {
		m_table = table;
	}
}
