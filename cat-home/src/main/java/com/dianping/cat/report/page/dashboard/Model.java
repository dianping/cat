package com.dianping.cat.report.page.dashboard;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_data;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getData() {
		return m_data;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public void setData(String data) {
		m_data = data;
	}

}
