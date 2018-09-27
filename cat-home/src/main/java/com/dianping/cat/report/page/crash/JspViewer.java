package com.dianping.cat.report.page.crash;

import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {

	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case APP_CRASH_LOG:
			return JspFile.APP_CRASH_LOG.getPath();
		case APP_CRASH_LOG_DETAIL:
			return JspFile.APP_CRASH_LOG_DETAIL.getPath();
		case APP_CRASH_GRAPH:
			return JspFile.APP_CRASH_GRAPH.getPath();
		case APP_CRASH_TREND:
			return JspFile.APP_CRASH_TREND.getPath();
		case APP_CRASH_LOG_JSON:
			return JspFile.APP_FETCH_DATA.getPath();
		case CRASH_STATISTICS:
			return JspFile.CRASH_STATISTICS.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
