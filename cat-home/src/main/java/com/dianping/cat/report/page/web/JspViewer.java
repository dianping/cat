package com.dianping.cat.report.page.web;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case JSON:
			return JspFile.JSON.getPath();
		case PROBLEM:
			return JspFile.PROBLEM.getPath();
		case PIECHART:
			return JspFile.PIECHART.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
