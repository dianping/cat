package com.dianping.cat.report.page.state;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY:
			return JspFile.HOURLY.getPath();
		case HISTORY:
			return JspFile.HISTORY.getPath();
		case GRAPH:
			return JspFile.GRAPH.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
