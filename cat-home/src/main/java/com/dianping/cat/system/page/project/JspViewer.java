package com.dianping.cat.system.page.project;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case DOMAINS:
		case PROJECT_UPDATE:
			return JspFile.JSON.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
