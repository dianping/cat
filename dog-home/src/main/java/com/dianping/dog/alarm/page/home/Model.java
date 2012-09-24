package com.dianping.dog.alarm.page.home;

import com.dianping.dog.alarm.AlarmPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<AlarmPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
