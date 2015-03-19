package com.dianping.cat.report.page.dependency;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case LINE_CHART:
			return JspFile.LINE_CHART.getPath();
		case TOPOLOGY:
			return JspFile.TOPOLOGY.getPath();
		case DEPENDENCY_DASHBOARD:
			return JspFile.DEPENDENCY_DASHBOARD.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
