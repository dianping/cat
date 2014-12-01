package com.dianping.cat.report.page.monitor;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case COUNT_API:
		case AVG_API:
		case SUM_API:
		case BATCH_API:
			return JspFile.VIEW.getPath();
		default:
			break;
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
