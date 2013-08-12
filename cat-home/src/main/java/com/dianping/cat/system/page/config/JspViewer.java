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
		//Node Config	
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_NODE_CONFIG_LIST.getPath();
		//Edge Config
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_EDGE_CONFIG_LIST.getPath();
		//Product Line
		case TOPOLOGY_GRAPH_PRODUCT_LINE:
			return JspFile.TOPOLOGY_GRAPH_PRODUCT_LINE.getPath();
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
			return JspFile.TOPOLOGY_GRAPH_PRODUCT_ADD_OR_UPDATE.getPath();
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
			return JspFile.TOPOLOGY_GRAPH_PRODUCT_LINE.getPath();
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			return JspFile.TOPOLOGY_GRAPH_PRODUCT_LINE.getPath();
		//Metric
		case METRIC_CONFIG_ADD_OR_UPDATE:
			return JspFile.METRIC_CONFIG_ADD_OR_UPDATE.getPath();
		case METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT:
			return JspFile.METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT.getPath();
		case METRIC_CONFIG_LIST:
			return JspFile.METRIC_CONFIG_LIST.getPath();
		case METRIC_CONFIG_DELETE:
			return JspFile.METRIC_CONFIG_LIST.getPath();
		//Excepton Config
		case EXCEPTION_THRESHOLDS:
			return JspFile.EXCEPTION_THRESHOLD.getPath();
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
			return JspFile.EXCEPTION_THRESHOLD.getPath();
		case EXCEPTION_THRESHOLD_DELETE:
			return JspFile.EXCEPTION_THRESHOLD.getPath();
		case EXCEPTION_THRESHOLD_UPDATE:
			return JspFile.EXCEPTION_THRESHOLD_CONFIG.getPath();
		//Bug
		case BUG_CONFIG_UPDATE:
			return JspFile.BUG_CONFIG_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
