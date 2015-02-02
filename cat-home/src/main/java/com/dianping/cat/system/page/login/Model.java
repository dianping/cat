package com.dianping.cat.system.page.login;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.LOGIN;
	}
}
