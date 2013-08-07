package com.dianping.cat.system.page.abtest;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case CREATE:
			return JspFile.CREATE.getPath();
		case AJAX_ADDGROUPSTRATEGY:
			return JspFile.AJAX.getPath();
		case AJAX_PARSEGROUPSTRATEGY:
			return JspFile.AJAX.getPath();
		case AJAX_CREATE:
			return JspFile.AJAX.getPath();
		case AJAX_DETAIL:
			return JspFile.AJAX.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		case REPORT:
			return JspFile.REPORT.getPath();
		case MODEL:
			return JspFile.MODEL.getPath();
		case ABTEST_CACULATOR:
			return JspFile.ABTEST_CACULATOR.getPath();

		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
