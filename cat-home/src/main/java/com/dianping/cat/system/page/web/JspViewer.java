package com.dianping.cat.system.page.web;

import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case SPEED_DELETE:
		case SPEED_LIST:
		case SPEED_SUBMIT:
			return JspFile.SPEED_LIST.getPath();
		case SPEED_UPDATE:
			return JspFile.SPEED_UPDATE.getPath();
		case JS_RULE_LIST:
		case JS_RULE_DELETE:
		case JS_RULE_UPDATE_SUBMIT:
			return JspFile.JS_RULE_LIST.getPath();
		case JS_RULE_UPDATE:
			return JspFile.JS_RULE_UPDATE.getPath();
		case WEB_RULE:
		case WEB_RULE_ADD_OR_UPDATE_SUBMIT:
		case WEB_RULE_DELETE:
			return JspFile.WEB_RULE.getPath();
		case WEB_RULE_ADD_OR_UPDATE:
			return JspFile.WEB_RULE_UPDATE.getPath();
		case WEB_CONSTANTS:
			return JspFile.WEB_CONSTANTS_LIST.getPath();
		case URL_PATTERN_CONFIG_UPDATE:
			return JspFile.URL_PATTERN_CONFIG_UPDATE.getPath();
		case URL_PATTERN_ALL:
			return JspFile.URL_PATTERN_ALL.getPath();
		case URL_PATTERN_DELETE:
			return JspFile.URL_PATTERN_ALL.getPath();
		case URL_PATTERN_UPDATE:
			return JspFile.URL_PATTERN_UPATE.getPath();
		case URL_PATTERN_UPDATE_SUBMIT:
			return JspFile.URL_PATTERN_ALL.getPath();
		case CODE_DELETE:
		case CODE_LIST:
		case CODE_SUBMIT:
			return JspFile.CODE_LIST.getPath();
		case CODE_UPDATE:
			return JspFile.CODE_UPDATE.getPath();
		case WEB_CONFIG_UPDATE:
			return JspFile.WEB_CONFIG_UPDATE.getPath();
		case WEB_SPEED_CONFIG_UPDATE:
			return JspFile.WEB_SPEED_CONFIG_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
