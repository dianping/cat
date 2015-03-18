package com.dianping.cat.report.page.app;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case LINECHART:
			return JspFile.VIEW.getPath();
		case PIECHART:
			return JspFile.PIECHART.getPath();
		case CONN_LINECHART:
			return JspFile.CONN_LINECHART.getPath();
		case CONN_PIECHART:
			return JspFile.CONN_PIECHART.getPath();
		case APP_ADD:
		case APP_DELETE:
			return JspFile.APP_MODIFY_RESULT.getPath();
		case LINECHART_JSON:
		case PIECHART_JSON:
		case APP_CONFIG_FETCH:
			return JspFile.APP_FETCH_DATA.getPath();
		case HOURLY_CRASH_LOG:
		case HISTORY_CRASH_LOG:
			return JspFile.CRASH_LOG.getPath();
		case SPEED:
			return JspFile.SPEED.getPath();
		case STATISTICS:
			return JspFile.STATISTICS.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
