package com.dianping.cat.report.page.storage;

import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_DATABASE:
		case HOURLY_CACHE:
			return JspFile.VIEW.getPath();
		case HOURLY_DATABASE_GRAPH:
		case HOURLY_CACHE_GRAPH:
			return JspFile.HOURL_GRAPH.getPath();
		case HISTORY_DATABASE:
		case HISTORY_CACHE:
			return JspFile.HISTORY_REPORT.getPath();
		case DASHBOARD:
			return JspFile.DASHBOARD.getPath();
		}
		throw new RuntimeException("Unknown action: " + action);
	}
}
