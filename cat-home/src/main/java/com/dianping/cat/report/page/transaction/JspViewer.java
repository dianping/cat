package com.dianping.cat.report.page.transaction;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		case GRAPHS:
			return JspFile.GRAPHS.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		case GROUP_GRAPHS:
			return JspFile.GROUP_GRAPHS.getPath();
		case HISTORY_GROUP_GRAPH:
			return JspFile.HISTORY_GROUP_GRAPH.getPath();
		case HISTORY_GROUP_REPORT:
			return JspFile.HISTORY_GROUP_REPORT.getPath();
		case HOURLY_GROUP_REPORT:
			return JspFile.HOURLY_GROUP_REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
