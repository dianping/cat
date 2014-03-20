package com.dianping.cat.report.page.alteration;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	
	private String m_status;
	private boolean m_isViewDataSuccess;
	
	public boolean isViewDataSuccess() {
		return m_isViewDataSuccess;
	}

	public void setViewDataSuccess(boolean isViewDataSuccess) {
		this.m_isViewDataSuccess = isViewDataSuccess;
	}

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		this.m_status = status;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
