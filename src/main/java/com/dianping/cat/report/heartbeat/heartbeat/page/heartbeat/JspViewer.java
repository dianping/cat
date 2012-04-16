package com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat;

import com.dianping.cat.report.heartbeat.heartbeat.HeartbeatPage;
import com.site.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<HeartbeatPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
