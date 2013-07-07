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
		case ADDABTEST:
			return JspFile.ADDABTEST.getPath();
		case ADDGROUPSTRATEGY:
			return JspFile.ADDGROUPSTRATEGY.getPath();
		case PARSEGROUPSTRATEGY:
			return JspFile.PARSEGROUPSTRATEGY.getPath();
		case AJAXADDABTEST:
			return JspFile.PARSEGROUPSTRATEGY.getPath();
		case AJAXDETAIL:
			return JspFile.PARSEGROUPSTRATEGY.getPath();
		case DETAIL:
			return JspFile.DETAIL.getPath();
		case REPORT:
			return JspFile.REPORT.getPath();
		case MODEL:
			return JspFile.MODEL.getPath();
			
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
