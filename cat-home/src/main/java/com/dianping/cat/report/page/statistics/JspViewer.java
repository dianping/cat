package com.dianping.cat.report.page.statistics;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case SERVICE_REPORT:
			return JspFile.SERVICE_REPORT.getPath();
		case SERVICE_HISTORY_REPORT:
			return JspFile.SERVICE_HISTORY_REPORT.getPath();
		case HEAVY_HISTORY_REPORT:
			return JspFile.HEAVY_HISTORY_REPORT.getPath();
		case HEAVY_REPORT:
			return JspFile.HEAVY_REPORT.getPath();
		case UTILIZATION_HISTORY_REPORT:
			return JspFile.UTILIZATION_HISTORY_REPORT.getPath();
		case UTILIZATION_REPORT:
			return JspFile.UTILIZATION_REPORT.getPath();
		case ALERT_SUMMARY:
			return JspFile.ALERT_SUMMARY.getPath();
		case JAR_REPORT:
			return JspFile.JAR_REPORT.getPath();
		case CLIENT_REPORT:
			return JspFile.CLIENT_REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
