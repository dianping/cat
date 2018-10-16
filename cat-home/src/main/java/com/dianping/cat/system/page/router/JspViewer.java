package com.dianping.cat.system.page.router;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case API:
		case JSON:
		case BUILD:
			return JspFile.API.getPath();
		case MODEL:
			return JspFile.MODEL.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
