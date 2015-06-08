package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class TopologyConfigProcessor {

	@Inject
	private ProductLineConfigManager m_productLineConfigManger;

	@Inject
	private GlobalConfigProcessor m_globalConfigManager;

	@Inject
	private TopologyGraphConfigManager m_topologyConfigManager;

	@Inject
	private TopoGraphFormatConfigManager m_formatConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private void graphEdgeConfigAdd(Payload payload, Model model) {
		String type = payload.getType();
		String from = payload.getFrom();
		String to = payload.getTo();
		EdgeConfig config = m_topologyConfigManager.queryEdgeConfig(type, from, to);

		model.setEdgeConfig(config);
	}

	private boolean graphEdgeConfigAddOrUpdateSubmit(Payload payload, Model model) {
		EdgeConfig config = payload.getEdgeConfig();

		if (!StringUtils.isEmpty(config.getType())) {
			model.setEdgeConfig(config);
			payload.setType(config.getType());
			return m_topologyConfigManager.insertEdgeConfig(config);
		} else {
			return false;
		}
	}

	private boolean graphEdgeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteEdgeConfig(payload.getType(), payload.getFrom(), payload.getTo());
	}

	private void graphNodeConfigAddOrUpdate(Payload payload, Model model) {
		String domain = payload.getDomain();
		String type = payload.getType();

		if (!StringUtils.isEmpty(domain)) {
			model.setDomainConfig(m_topologyConfigManager.queryNodeConfig(type, domain));
		}
	}

	private boolean graphNodeConfigAddOrUpdateSubmit(Payload payload, Model model) {
		String type = payload.getType();
		DomainConfig config = payload.getDomainConfig();
		String domain = config.getId();
		model.setDomainConfig(config);

		if (Constants.ALL.equalsIgnoreCase(domain)) {
			return m_topologyConfigManager.insertDomainDefaultConfig(type, config);
		} else {
			return m_topologyConfigManager.insertDomainConfig(type, config);
		}
	}

	private boolean graphNodeConfigDelete(Payload payload) {
		return m_topologyConfigManager.deleteDomainConfig(payload.getType(), payload.getDomain());
	}

	private Pair<Boolean, String> graphProductLineConfigAddOrUpdateSubmit(Payload payload, Model model) {
		ProductLine line = payload.getProductLine();
		String[] domains = payload.getDomains();
		String type = payload.getType();

		return m_productLineConfigManger.insertProductLine(line, domains, type);
	}

	private void graphPruductLineAddOrUpdate(Payload payload, Model model) {
		String name = payload.getProductLineName();
		String type = payload.getType();

		if (!StringUtils.isEmpty(name)) {
			model.setProductLine(m_productLineConfigManger.findProductLine(name, type));
		}
	}

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
			graphNodeConfigAddOrUpdate(payload, model);
			model.setProjects(m_globalConfigManager.queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphNodeConfigAddOrUpdateSubmit(payload, model));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
			model.setOpState(graphNodeConfigDelete(payload));
			model.setConfig(m_topologyConfigManager.getConfig());
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.buildEdgeInfo();
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
			graphEdgeConfigAdd(payload, model);
			model.setProjects(m_globalConfigManager.queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(graphEdgeConfigAddOrUpdateSubmit(payload, model));
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.buildEdgeInfo();
			break;
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
			model.setGraphConfig(m_topologyConfigManager.getConfig());
			model.setOpState(graphEdgeConfigDelete(payload));
			model.buildEdgeInfo();
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE:
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE:
			graphPruductLineAddOrUpdate(payload, model);
			model.setProjects(m_globalConfigManager.queryAllProjects());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_DELETE:
			model.setOpState(m_productLineConfigManger.deleteProductLine(payload.getProductLineName(), payload.getType()));
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
			break;
		case TOPOLOGY_GRAPH_PRODUCT_LINE_ADD_OR_UPDATE_SUBMIT:
			Pair<Boolean, String> addProductlineResult = graphProductLineConfigAddOrUpdateSubmit(payload, model);
			String duplicateDomains = addProductlineResult.getValue();

			model.setOpState(addProductlineResult.getKey());
			if (!StringUtils.isEmpty(duplicateDomains)) {
				model.setDuplicateDomains(addProductlineResult.getValue());
			}
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());
			model.setTypeToProductLines(m_productLineConfigManger.queryTypeProductLines());
			break;
		case TOPO_GRAPH_FORMAT_CONFIG_UPDATE:
			String topoGraphFormat = payload.getContent();
			if (!StringUtils.isEmpty(topoGraphFormat)) {
				model.setOpState(m_formatConfigManager.insert(topoGraphFormat));
			}
			model.setContent(m_configHtmlParser.parse(m_formatConfigManager.getConfig().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

}
