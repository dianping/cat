package com.dianping.cat.system.page.config.processor;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.web.js.AggregationConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.report.alert.web.WebRuleConfigManager;
import com.dianping.cat.report.page.web.CityManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class WebConfigProcessor extends BaseProcesser {

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	private AggregationConfigManager m_aggreationConfigManager;

	@Inject
	private WebRuleConfigManager m_webRuleConfigManager;

	@Inject
	private CityManager m_cityManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private void buildWebConfigInfo(Model model) {
		Map<Integer, PatternItem> patterns = m_urlPatternConfigManager.getId2Items();

		model.setCities(m_appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setOperators(m_appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPatternItems(patterns);
		model.setWebCodes(m_urlPatternConfigManager.getUrlPattern().getCodes());
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
		case URL_PATTERN_CONFIG_UPDATE:
			String config = payload.getContent();

			if (!StringUtils.isEmpty(config)) {
				model.setOpState(m_urlPatternConfigManager.insert(config));
			}
			model.setContent(m_configHtmlParser.parse(m_urlPatternConfigManager.getUrlPattern().toString()));
			break;
		case URL_PATTERN_ALL:
			model.setPatternItems(m_urlPatternConfigManager.getId2Items());
			break;
		case URL_PATTERN_UPDATE:
			model.setPatternItem(m_urlPatternConfigManager.queryUrlPattern(payload.getKey()));
			break;
		case URL_PATTERN_UPDATE_SUBMIT:
			try {
				String key = payload.getKey();
				PatternItem patternItem = payload.getPatternItem();

				if (m_urlPatternConfigManager.queryUrlPatterns().containsKey(key)) {
					int id = payload.getId();

					patternItem.setId(id);
					m_urlPatternConfigManager.updatePatternItem(patternItem);
				} else {
					m_urlPatternConfigManager.insertPatternItem(patternItem);
				}
				model.setPatternItems(m_urlPatternConfigManager.getId2Items());
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case URL_PATTERN_DELETE:
			m_urlPatternConfigManager.deletePatternItem(payload.getKey());
			model.setPatternItems(m_urlPatternConfigManager.getId2Items());
			break;
		case WEB_RULE:
			buildWebConfigInfo(model);
			model.setRules(m_webRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case WEB_RULE_ADD_OR_UPDATE:
			buildWebConfigInfo(model);
			generateRuleConfigContent(payload.getRuleId(), m_webRuleConfigManager, model);
			break;
		case WEB_RULE_ADD_OR_UPDATE_SUBMIT:
			buildWebConfigInfo(model);
			model.setRules(m_webRuleConfigManager.getMonitorRules().getRules().values());
			model.setOpState(addSubmitRule(m_webRuleConfigManager, payload.getRuleId(), "", payload.getConfigs()));
			break;
		case WEB_RULE_DELETE:
			buildWebConfigInfo(model);
			model.setRules(m_webRuleConfigManager.getMonitorRules().getRules().values());
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
