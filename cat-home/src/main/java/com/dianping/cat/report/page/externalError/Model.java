package com.dianping.cat.report.page.externalError;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
