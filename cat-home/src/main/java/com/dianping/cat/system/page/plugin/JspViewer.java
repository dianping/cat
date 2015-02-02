package com.dianping.cat.system.page.plugin;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
