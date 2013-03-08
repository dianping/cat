package com.dianping.cat.report.page.cache;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		default:
			break;
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
