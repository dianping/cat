package com.dianping.cat.report.page.alert;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private String m_alertResult;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getAlertResult() {
		return m_alertResult;
	}

	@Override
	public Action getDefaultAction() {
		return Action.ALERT;
	}

	public void setAlertResult(String alertResult) {
		m_alertResult = alertResult;
	}
}
