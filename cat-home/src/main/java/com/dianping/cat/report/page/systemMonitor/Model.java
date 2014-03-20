package com.dianping.cat.report.page.systemMonitor;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.SYSTEM_API;
	}
}
