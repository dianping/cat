package com.dianping.cat.report.page.top;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case API:
			return JspFile.API.getPath();
		case HEALTH:
			return JspFile.API.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
