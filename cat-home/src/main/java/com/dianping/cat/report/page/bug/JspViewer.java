package com.dianping.cat.report.page.bug;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HTTP_JSON:
			return JspFile.HTTP_JSON.getPath();
		case SERVICE_REPORT:
			return JspFile.SERVICE_REPORT.getPath();
		case SERVICE_HISTORY_REPORT:
			return JspFile.SERVICE_HISTORY_REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
