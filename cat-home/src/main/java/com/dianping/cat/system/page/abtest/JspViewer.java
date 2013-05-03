package com.dianping.cat.system.page.abtest;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case LIST:
			return JspFile.ALLTESTVIEW.getPath();
		case REPORT:
			return JspFile.REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
