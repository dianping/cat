package com.dianping.cat.agent.core.page.index;

import com.dianping.cat.agent.core.CorePage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<CorePage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
