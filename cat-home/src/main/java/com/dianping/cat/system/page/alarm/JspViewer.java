package com.dianping.cat.system.page.alarm;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case ALARM_TEMPLATE_LIST:
			return JspFile.ALARM_TEMPLATE_LIST.getPath();
		case ALARM_RECORD_LIST:
			return JspFile.ALARM_RECORD_LIST.getPath();
		case ALARM_RULE_ADD:
			return JspFile.ALARM_RULE_ADD.getPath();
		case ALARM_RULE_ADD_SUBMIT:
			return JspFile.ALARM_RULE_ADD_SUBMIT.getPath();
		case EXCEPTION_ALARM_RULE_DELETE:
			return JspFile.EXCEPTION_ALARM_RULE_DELETE.getPath();
		case EXCEPTION_ALARM_RULE_LIST:
			return JspFile.EXCEPTION_ALARM_RULE_LIST.getPath();
		case ALARM_TEMPLATE_ADD:
			return JspFile.ALARM_TEMPLATE_ADD.getPath();
		case ALARM_TEMPLATE_ADD_SUBMIT:
			return JspFile.ALARM_TEMPLATE_ADD_SUBMIT.getPath();
		case ALARM_TEMPLATE_DELETE:
			return JspFile.ALARM_TEMPLATE_DELETE.getPath();
		case ALARM_TEMPLATE_UPDATE:
			return JspFile.ALARM_TEMPLATE_UPDATE.getPath();
		case ALARM_TEMPLATE_UPDATE_SUBMIT:
			return JspFile.ALARM_TEMPLATE_UPDATE_SUBMIT.getPath();
		case ALARM_RULE_UPDATE:
			return JspFile.ALARM_RULE_UPDATE.getPath();
		case ALARM_RULE_UPDATE_SUBMIT:
			return JspFile.ALARM_RULE_UPDATE_SUBMIT.getPath();
		case EXCEPTION_ALARM_RULE_SUB:
			return JspFile.EXCEPTION_ALARM_RULE_SUB.getPath();
		case SERVICE_ALARM_RULE_DELETE:
			return JspFile.SERVICE_ALARM_RULE_DELETE.getPath();
		case SERVICE_ALARM_RULE_SUB:
			return JspFile.SERVICE_ALARM_RULE_SUB.getPath();
		case SERVICE_ALARM_RULE_LIST:
			return JspFile.SERVICE_ALARM_RULE_LIST.getPath();
		case ALARM_RECORD_DETAIL:
			return JspFile.ALARM_RECORD_DETAIL.getPath();
		case SCHEDULED_REPORT_ADD:
			return JspFile.SCHEDULED_REPORT_ADD.getPath();
		case SCHEDULED_REPORT_ADD_SUBMIT:
			return JspFile.SCHEDULED_REPORT_ADD_SUBMIT.getPath();
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
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
