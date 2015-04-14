package com.dianping.cat.report.page.storage;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_STORAGE:
			return JspFile.VIEW.getPath();
		case HOURLY_STORAGE_GRAPH:
			return JspFile.HOURL_GRAPH.getPath();
		case HISTORY_STORAGE:
			return JspFile.HISTORY_REPORT.getPath();
		case DASHBOARD:
			return JspFile.DASHBOARD.getPath();
		}
		throw new RuntimeException("Unknown action: " + action);
	}
}
