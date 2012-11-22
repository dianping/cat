package com.dianping.cat.report.page.heartbeat;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case MOBILE:
			return JspFile.MOBILE.getPath();
		case HISTORY:
			return JspFile.HISTORY.getPath();

		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
