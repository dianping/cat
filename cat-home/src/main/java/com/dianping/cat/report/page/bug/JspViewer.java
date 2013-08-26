package com.dianping.cat.report.page.bug;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case BUG_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		case BUG_HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case BUG_HTTP_JSON:
			return JspFile.HTTP_JSON.getPath();
		case SERVICE_REPORT:
			return JspFile.SERVICE_REPORT.getPath();
		case SERVICE_HISTORY_REPORT:
			return JspFile.SERVICE_HISTORY_REPORT.getPath();
		case HEAVY_HISTORY_REPORT:
			return JspFile.HEAVY_HISTORY_REPORT.getPath();
		case HEAVY_REPORT:
			return JspFile.HEAVY_REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
