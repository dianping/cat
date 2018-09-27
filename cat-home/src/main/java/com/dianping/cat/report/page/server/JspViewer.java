package com.dianping.cat.report.page.server;

import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case GRAPH:
			return JspFile.GRAPH.getPath();
		case SCREEN:
			return JspFile.SCREEN.getPath();
		case AGGREGATE:
			return JspFile.AGGREGATE.getPath();
		case ENDPOINT:
		case MEASUREMTN:
		case BUILDVIEW:
		case VIEW_JSON:
		case GRAPH_JSON:
		case SCREEN_JSON:
			return JspFile.JSON.getPath();
		case SCREENS:
		case SCREEN_SUBMIT:
		case SCREEN_DELETE:
		case GRAPH_SUBMIT:
			return JspFile.SCREENS.getPath();
		case SCREEN_UPDATE:
			return JspFile.SCREEN_UPDATE.getPath();
		case GRAPH_UPDATE:
			return JspFile.SCREEN_CONFIG_UPDATE.getPath();
		case INFLUX_CONFIG_UPDATE:
			return JspFile.INFLUX_CONFIG_UPDATE.getPath();
		case SERVER_METRIC_CONFIG_UPDATE:
			return JspFile.SERVER_METRIC_CONFIG_UPDATE.getPath();
		case SERVER_ALARM_RULE:
		case SERVER_ALARM_RULE_DELETE:
		case SERVER_ALARM_RULE_SUBMIT:
			return JspFile.SERVER_ALARM_RULE.getPath();
		case SERVER_ALARM_RULE_UPDATE:
			return JspFile.SERVER_ALARM_RULE_UPDATE.getPath();
		case NET_GRAPH_CONFIG_UPDATE:
			return JspFile.NET_GRAPH_CONFIG_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
