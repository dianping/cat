package com.dianping.cat.system.page.project;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case ALL:
			return JspFile.ALL.getPath();
		case PROJECT_UPDATE:
			return JspFile.UPATE.getPath();
		case PROJECT_UPDATE_SUBMIT:
			return JspFile.ALL.getPath();
		default:
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
