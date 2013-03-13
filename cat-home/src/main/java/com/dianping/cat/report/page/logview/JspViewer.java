package com.dianping.cat.report.page.logview;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();
		Payload payload = ctx.getPayload();

		switch (action) {
		case VIEW:
			if (payload.isShowHeader()) {
				return JspFile.LOGVIEW.getPath();
			} else {
				return JspFile.LOGVIEW_NO_HEADER.getPath();
			}
		case MOBILE:
			return JspFile.MOBILE.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
