package com.dianping.cat.report.page.browser;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case JS_ERROR:
			return JspFile.JS_ERROR.getPath();
		case JS_ERROR_DETAIL:
			return JspFile.JS_ERROR_DETAIL.getPath();
		case AJAX_LINECHART:
			return JspFile.AJAX_LINECHART.getPath();
		case AJAX_PIECHART:
			return JspFile.AJAX_PIECHART.getPath();
		case SPEED:
			return JspFile.SPEED.getPath();
		case SPEED_GRAPH:
			return JspFile.SPEED_GRAPH.getPath();
		case SPEED_CONFIG_FETCH:
		case SPEED_JSON:
			return JspFile.FETCH_DATA.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
