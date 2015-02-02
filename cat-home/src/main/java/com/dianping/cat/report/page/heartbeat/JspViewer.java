package com.dianping.cat.report.page.heartbeat;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case HISTORY:
			return JspFile.HISTORY.getPath();
		case PART_HISTORY:
			return JspFile.PART_HISTORY.getPath();

		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
