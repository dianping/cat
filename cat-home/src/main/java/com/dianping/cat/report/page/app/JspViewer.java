package com.dianping.cat.report.page.app;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case PIECHART:
			return JspFile.PIECHART.getPath();
		case APP_ADD:
		case APP_DELETE:
			return JspFile.APP_MODIFY_RESULT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
