package com.dianping.cat.report.page.historyReport;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case TRANSACTION:
			return JspFile.TRANSACTION.getPath();
		case EVENT:
			return JspFile.EVENT.getPath();
		case PROBLEM:
			return JspFile.PROBLEM.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
