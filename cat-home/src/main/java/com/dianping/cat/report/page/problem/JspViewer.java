package com.dianping.cat.report.page.problem;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
