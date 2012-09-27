package com.dianping.cat.system.page.alarm;

import com.dianping.cat.system.SystemPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	public String getDomain(){
		return "";
	}
	public String getDate(){
		return "";
	}
}
