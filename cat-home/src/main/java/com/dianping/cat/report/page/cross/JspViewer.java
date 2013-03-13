package com.dianping.cat.report.page.cross;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_PROJECT:
			return JspFile.HOURLY_PROJECT.getPath();
		case HOURLY_HOST:
			return JspFile.HOURLY_HOST.getPath();
		case HOURLY_METHOD:
			return JspFile.HOURLY_METHOD.getPath();
		case HISTORY_HOST:
			return JspFile.HISTORY_HOST.getPath();
		case HISTORY_METHOD:
			return JspFile.HISTORY_METHOD.getPath();
		case HISTORY_PROJECT:
			return JspFile.HISTORY_PROJECT.getPath();
		case METHOD_QUERY:
			return JspFile.METHOD_QUERY.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
