package com.dianping.cat.report.page.monitor;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_status;

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.COUNT_API;
	}
}
