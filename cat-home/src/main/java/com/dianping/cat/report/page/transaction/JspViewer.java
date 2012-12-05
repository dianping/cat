package com.dianping.cat.report.page.transaction;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		case GRAPHS:
			return JspFile.GRAPHS.getPath();
		case MOBILE:
			return JspFile.MOBILE.getPath();
		case MOBILE_GRAPHS:
			return JspFile.MOBILE_GRAPHS.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
