package com.dianping.cat.system.page.login;

import com.dianping.cat.system.SystemPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.LOGIN;
	}
}
