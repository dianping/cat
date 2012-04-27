package com.dianping.cat.report.page.problem;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case GROUP:
			return JspFile.GROUP.getPath();
		case THREAD:
			return JspFile.THREAD.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		case MOBILE:
			return JspFile.MOBILE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
