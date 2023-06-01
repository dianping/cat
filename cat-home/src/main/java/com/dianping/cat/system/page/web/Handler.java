package com.dianping.cat.system.page.web;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.report.alert.browser.AjaxRuleConfigManager;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.spi.config.BaseRuleConfigManager;
import com.dianping.cat.report.page.browser.ModuleManager;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private WebSpeedConfigManager m_webSpeedConfigManager;

	@Inject
	private ConfigModificationDao m_configModificationDao;

	@Inject
	private JsRuleConfigManager m_jsRuleConfigManager;

	@Inject
	private ModuleManager m_moduleManager;

	@Inject
	private WebConfigManager m_appConfigManager;

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	private AjaxRuleConfigManager m_webRuleConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	protected RuleFTLDecorator m_ruleDecorator;

	public boolean addSubmitRule(BaseRuleConfigManager manager, String id, String metrics, String configs) {
		try {
			String xmlContent = manager.updateRule(id, metrics, configs);

			return manager.insert(xmlContent);
		} catch (Exception ex) {
			Cat.logError(ex);
			return false;
		}
	}

	private void buildWebConfigInfo(Model model) {
		Map<Integer, PatternItem> patterns = m_urlPatternConfigManager.getId2Items();

		model.setWebCities(m_appConfigManager.queryConfigItem(WebConfigManager.CITY));
		model.setWebOperators(m_appConfigManager.queryConfigItem(WebConfigManager.OPERATOR));
		model.setPatternItems(patterns);
		model.setWebCodes(m_urlPatternConfigManager.queryCodes());
		model.setWebNetworks(m_appConfigManager.queryConfigItem(WebConfigManager.NETWORK));
	}

	public boolean deleteRule(BaseRuleConfigManager manager, String key) {
		try {
			String xmlContent = manager.deleteRule(key);
			return manager.insert(xmlContent);
		} catch (Exception ex) {
			return false;
		}
	}

	public void generateRuleConfigContent(String key, BaseRuleConfigManager manager, Model model) {
		String configsStr = "";
		String ruleId = "";

		if (StringUtils.isNotEmpty(key)) {
			Rule rule = manager.queryRule(key);

			if (rule != null) {
				ruleId = rule.getId();
				configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
				String configHeader = new DefaultJsonBuilder(true).buildArray(rule.getMetricItems());

				model.setConfigHeader(configHeader);
			}
		}
		String content = m_ruleDecorator.generateConfigsHtml(configsStr);

		model.setContent(content);
		model.setId(ruleId);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "web")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "web")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.WEB);
		storeModifyInfo(ctx, payload);

		Action action = payload.getAction();
		model.setAction(action);

		switch (action) {
		case SPEED_DELETE:
			String webpage = payload.getWebPage();
			int pageId = m_webSpeedConfigManager.querySpeedId(webpage);
			m_webSpeedConfigManager.deleteSpeed(pageId);
			model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
			break;
		case SPEED_LIST:
			String name = payload.getWebPage();

			if (StringUtils.isNotEmpty(name)) {
				Speed speed = m_webSpeedConfigManager.querySpeed(name.split("\\|")[1]);
				model.setSpeed(speed);
			}
			model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
			break;
		case SPEED_SUBMIT:
			speedConfigSubmit(model, payload);
			break;
		case SPEED_UPDATE:
			queryStep(model, payload);
			break;
		case JS_RULE_LIST:
			model.setJsRules(m_jsRuleConfigManager.queryAllExceptionLimits());
			break;
		case JS_RULE_DELETE:
			m_jsRuleConfigManager.deleteExceptionLimit(payload.getRuleId());
			model.setJsRules(m_jsRuleConfigManager.queryAllExceptionLimits());
			break;
		case JS_RULE_UPDATE:
			model.setModules(m_moduleManager.getModules());
			model.setJsRule(m_jsRuleConfigManager.queryExceptionLimit(payload.getRuleId()));
			break;
		case JS_RULE_UPDATE_SUBMIT:
			m_jsRuleConfigManager.insertExceptionLimit(payload.getJsRule());
			model.setJsRules(m_jsRuleConfigManager.queryAllExceptionLimits());
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
		case WEB_CONSTANTS:
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
		case CODE_DELETE:
			m_urlPatternConfigManager.removeCode(payload.getId());
			model.setWebCodes(m_urlPatternConfigManager.queryCodes());
			break;
		case CODE_LIST:
			model.setWebCodes(m_urlPatternConfigManager.queryCodes());
			break;
		case CODE_SUBMIT:
			codeSubmit(model, payload);
			break;
		case CODE_UPDATE:
			Map<Integer, Code> codes = m_urlPatternConfigManager.queryCodes();
			Code code = codes.get(payload.getId());

			model.setCode(code);
			model.setWebCodes(codes);
			break;
		case WEB_CONFIG_UPDATE:
			String appConfig = payload.getContent();
			if (!StringUtils.isEmpty(appConfig)) {
				model.setOpState(m_appConfigManager.insert(appConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_appConfigManager.getConfig().toString()));
			break;
		case WEB_SPEED_CONFIG_UPDATE:
			String speedConfig = payload.getContent();
			if (!StringUtils.isEmpty(speedConfig)) {
				model.setOpState(m_webSpeedConfigManager.insert(speedConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_webSpeedConfigManager.getConfig().toString()));
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void speedConfigSubmit(Model model, Payload payload) {
		Step step = payload.getStep();
		String page = step.getPage();
		Speed speed = m_webSpeedConfigManager.querySpeed(page);

		if (speed == null) {
			int id = m_webSpeedConfigManager.generateSpeedId();
			speed = new Speed();
			speed.setId(id);
			speed.setPage(page);
		}

		for (int i = 1; i <= Constants.MAX_SPEED_POINT; i++) {
			String title = step.getStep(i);

			if (StringUtils.isNotEmpty(title)) {
				com.dianping.cat.configuration.web.speed.entity.Step internalStep = new com.dianping.cat.configuration.web.speed.entity.Step();
				internalStep.setId(i);
				internalStep.setTitle(title);

				speed.addStep(internalStep);
			} else {
				speed.removeStep(i);
			}
		}

		m_webSpeedConfigManager.updateConfig(speed);
		model.setSpeed(speed);
		model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
	}

	private void codeSubmit(Model model, Payload payload) {
		try {
			Code code = payload.getCode();
			m_urlPatternConfigManager.updateCode(code.getId(), code);
		} catch (Exception e) {
			Cat.logError(e);
		}
		model.setWebCodes(m_urlPatternConfigManager.queryCodes());
	}

	private void queryStep(Model model, Payload payload) {
		String page = payload.getWebPage();
		Step step = new Step();

		if (page != null) {
			Speed speed = m_webSpeedConfigManager.querySpeed(page);

			if (speed != null) {
				step.setPageid(speed.getId());
				step.setPage(speed.getPage());

				for (int i = 1; i <= Constants.MAX_SPEED_POINT; i++) {
					com.dianping.cat.configuration.web.speed.entity.Step internalStep = speed.findStep(i);

					if (internalStep != null) {
						step.setStep(i, internalStep.getTitle());
					}
				}
			}
		}

		model.setStep(step);
	}

	public void store(String userName, String accountName, Payload payload) {
		ConfigModification modification = m_configModificationDao.createLocal();

		modification.setUserName(userName);
		modification.setAccountName(accountName);
		modification.setActionName(payload.getAction().getName());
		modification.setDate(new Date());
		modification.setArgument(new JsonBuilder().toJson(payload));

		try {
			m_configModificationDao.insert(modification);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}

	private void storeModifyInfo(Context ctx, Payload payload) {
		Cookie cookie = ctx.getCookie("ct");

		if (cookie != null) {
			String cookieValue = cookie.getValue();

			try {
				String[] values = cookieValue.split("\\|");
				String userName = values[0];
				String account = values[1];

				if (userName.startsWith("\"")) {
					userName = userName.substring(1, userName.length() - 1);
				}
				userName = URLDecoder.decode(userName, "UTF-8");

				store(userName, account, payload);
			} catch (Exception ex) {
				Cat.logError("store cookie fail:" + cookieValue, new RuntimeException());
			}
		}
	}
}
