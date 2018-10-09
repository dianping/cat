package com.dianping.cat.system.page.permission;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case USER:
			return JspFile.USER.getPath();
		case RESOURCE:
			return JspFile.RESOURCE.getPath();
		case ERROR:
			return JspFile.ERROR.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
