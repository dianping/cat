package com.dianping.cat.report.page.database;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
