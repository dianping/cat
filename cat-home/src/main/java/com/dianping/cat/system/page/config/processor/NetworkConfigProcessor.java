package com.dianping.cat.system.page.config.processor;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.network.NetworkRuleConfigManager;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class NetworkConfigProcessor extends BaseProcesser {

	@Inject
	private NetworkRuleConfigManager m_networkRuleConfigManager;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {

		case NETWORK_RULE_CONFIG_LIST:
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getKey(), m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_networkRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case NETWORK_RULE_DELETE:
			model.setOpState(deleteRule(m_networkRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_networkRuleConfigManager, model);
			break;
		case NET_GRAPH_CONFIG_UPDATE:
			String netGraphConfig = payload.getContent();
			if (!StringUtils.isEmpty(netGraphConfig)) {
				model.setOpState(m_netGraphConfigManager.insert(netGraphConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_netGraphConfigManager.getConfig().toString()));
			break;

		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
