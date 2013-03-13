package com.dianping.cat.report.page.ip;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case MOBILE:
			return JspFile.MOBILE.getPath();
		case MOBILE_IP:
			return JspFile.MOBILE_IP.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
