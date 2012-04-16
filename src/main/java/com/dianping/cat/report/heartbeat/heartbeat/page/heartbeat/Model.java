package com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat;

import com.dianping.cat.report.heartbeat.heartbeat.HeartbeatPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<HeartbeatPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
