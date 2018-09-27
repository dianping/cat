package com.dianping.cat.report.page.applog;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case APP_LOG:
			return JspFile.APP_LOG.getPath();
		case APP_LOG_DETAIL:
			return JspFile.APP_LOG_DETAIL.getPath();
		case APP_LOG_GRAPH:
			return JspFile.APP_LOG_GRAPH.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
