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
		case DASHBOARD:
			return JspFile.DASHBOARD.getPath();
		case PRODUCT_LINE:
			return JspFile.PRODUCT_LINE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
