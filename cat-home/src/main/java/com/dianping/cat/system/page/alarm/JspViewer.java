package com.dianping.cat.system.page.alarm;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case SCHEDULED_REPORT_DELETE:
			return JspFile.SCHEDULED_REPORT_DELETE.getPath();
		case SCHEDULED_REPORT_LIST:
			return JspFile.SCHEDULED_REPORT_LIST.getPath();
		case SCHEDULED_REPORT_UPDATE:
			return JspFile.SCHEDULED_REPORT_UPDATE.getPath();
		case SCHEDULED_REPORT_UPDATE_SUBMIT:
			return JspFile.SCHEDULED_REPORT_UPDATE_SUBMIT.getPath();
		case SCHEDULED_REPORT_SUB:
			return JspFile.SCHEDULED_REPORT_SUB.getPath();
		case REPORT_RECORD_LIST:
			return JspFile.ALARM_RECORD_LIST.getPath();
		case ALARM_RECORD_DETAIL:
			return JspFile.ALARM_RECORD_DETAIL.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
