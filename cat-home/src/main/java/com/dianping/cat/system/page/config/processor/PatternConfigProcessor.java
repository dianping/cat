package com.dianping.cat.system.page.config.processor;

import java.util.Collection;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.aggreation.model.entity.AggregationRule;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.report.alert.web.WebRuleConfigManager;
import com.dianping.cat.report.page.web.CityManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class PatternConfigProcessor extends BaseProcesser {

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	private AggregationConfigManager m_aggreationConfigManager;

	@Inject
	private WebRuleConfigManager m_webRuleConfigManager;

	@Inject
	private CityManager m_cityManager;

	private void buildWebConfigInfo(Model model) {
		Collection<PatternItem> patterns = m_urlPatternConfigManager.queryUrlPatternRules();
		model.setPatternItems(patterns);
		model.setCityInfos(m_cityManager.getCities());
		model.setRules(m_webRuleConfigManager.getMonitorRules().getRules().values());
	}

	private void deleteAggregationRule(Payload payload) {
		m_aggreationConfigManager.deleteAggregationRule(payload.getPattern());
	}

	public void processPatternConfig(Action action, Payload payload, Model model) {
		switch (action) {
		case AGGREGATION_ALL:
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case AGGREGATION_UPDATE:
			model.setAggregationRule(m_aggreationConfigManager.queryAggration(payload.getPattern()));
			break;
		case AGGREGATION_UPDATE_SUBMIT:
			updateAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case AGGREGATION_DELETE:
			deleteAggregationRule(payload);
			model.setAggregationRules(m_aggreationConfigManager.queryAggregationRules());
			break;
		case URL_PATTERN_ALL:
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case URL_PATTERN_UPDATE:
			model.setPatternItem(m_urlPatternConfigManager.queryUrlPattern(payload.getKey()));
			break;
		case URL_PATTERN_UPDATE_SUBMIT:
			m_urlPatternConfigManager.insertPatternItem(payload.getPatternItem());
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case URL_PATTERN_DELETE:
			m_urlPatternConfigManager.deletePatternItem(payload.getKey());
			model.setPatternItems(m_urlPatternConfigManager.queryUrlPatternRules());
			break;
		case WEB_RULE:
			buildWebConfigInfo(model);
			break;
		case WEB_RULE_ADD_OR_UPDATE:
			buildWebConfigInfo(model);
			generateRuleConfigContent(payload.getRuleId(), m_webRuleConfigManager, model);
			break;
		case WEB_RULE_ADD_OR_UPDATE_SUBMIT:
			buildWebConfigInfo(model);
			model.setOpState(addSubmitRule(m_webRuleConfigManager, payload.getRuleId(), "", payload.getConfigs()));
			break;
		case WEB_RULE_DELETE:
			buildWebConfigInfo(model);
			model.setOpState(deleteRule(m_webRuleConfigManager, payload.getRuleId()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private void updateAggregationRule(Payload payload) {
		AggregationRule proto = payload.getRule();
		m_aggreationConfigManager.insertAggregationRule(proto);
	}
}
