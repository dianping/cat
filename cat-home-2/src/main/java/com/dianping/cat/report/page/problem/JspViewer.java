package com.dianping.cat.report.page.problem;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOULY_REPORT:
			return JspFile.ALL.getPath();
		case GROUP:
			return JspFile.GROUP.getPath();
		case THREAD:
			return JspFile.THREAD.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		case HOUR_GRAPH:
			return JspFile.HOUR_GRAPH.getPath();
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
