package com.dianping.cat.system.page.config;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case PROJECT_ALL:
			return JspFile.PROJECT_ALL.getPath();
		case PROJECT_UPDATE:
			return JspFile.PROJECT_UPATE.getPath();
		case PROJECT_UPDATE_SUBMIT:
			return JspFile.PROJECT_ALL.getPath();
		case AGGREGATION_ALL:
			return JspFile.AGGREGATION_ALL.getPath();
		case AGGREGATION_DELETE:
			return JspFile.AGGREGATION_ALL.getPath();
		case AGGREGATION_UPDATE:
			return JspFile.AGGREGATION_UPATE.getPath();
		case AGGREGATION_UPDATE_SUBMIT:
			return JspFile.AGGREGATION_ALL.getPath();
		case TOPOLOGY_GRAPH_CONFIG_NODE_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_ADD_OR_SUBMIT.getPath();
		case TOPOLOGY_GRAPH_CONFIG_NODE_DELETE:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_DELETE.getPath();
		case TOPOLOGY_GRAPH_CONFIG_EDGE_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_EDGE_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_CONFIG_EDGE_DELETE:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_EDGE_DELETE.getPath();
		case TOPOLOGY_GRAPH_CONFIG_LIST:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_CONFIG_NODE_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_CONFIG_NODE_ADD_OR_UPDATE_SUBMIT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
