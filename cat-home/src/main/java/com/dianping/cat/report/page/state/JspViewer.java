package com.dianping.cat.report.page.state;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY:
			return JspFile.HOURLY.getPath();
		case HISTORY:
			return JspFile.HISTORY.getPath();
			
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
