package com.dianping.cat.report.page.task;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case REDO:
			return JspFile.REDO.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
