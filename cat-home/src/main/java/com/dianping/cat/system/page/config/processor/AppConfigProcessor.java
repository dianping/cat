package com.dianping.cat.system.page.config.processor;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;
import com.site.helper.Splitters;

public class AppConfigProcessor extends BaseProcesser {

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppComparisonConfigManager m_appComparisonConfigManager;

	private void buildAppConfigInfo(AppConfigManager appConfigManager, Model model) {
		model.setConnectionTypes(appConfigManager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(appConfigManager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(appConfigManager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(appConfigManager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(appConfigManager.queryCommands());
	}

	public void process(Action action, Payload payload, Model model) {
		int id;

		switch (action) {
		case APP_LIST:
			buildListInfo(model, payload);
			break;
		case APP_UPDATE:
			id = payload.getId();
			Command command = m_appConfigManager.getConfig().findCommand(id);

			if (command == null) {
				command = new Command();
			}
			model.setUpdateCommand(command);
			break;
		case APP_SUBMIT:
			id = payload.getId();
			String domain = payload.getDomain();
			String name = payload.getName();
			String title = payload.getTitle();

			if (m_appConfigManager.containCommand(id)) {
				if (m_appConfigManager.updateCommand(id, domain, name, title)) {
					model.setOpState(true);
				} else {
					model.setOpState(false);
				}
			} else {
				try {
					if (m_appConfigManager.addCommand(domain, title, name).getKey()) {
						model.setOpState(true);
					} else {
						model.setOpState(false);
					}
				} catch (Exception e) {
					model.setOpState(false);
				}
			}
			buildListInfo(model, payload);
			break;
		case APP_PAGE_DELETE:
			id = payload.getId();

			if (m_appConfigManager.deleteCommand(id)) {
				model.setOpState(true);
			} else {
				model.setOpState(false);
			}
			buildListInfo(model, payload);
			break;
		case APP_CODE_UPDATE:
			id = payload.getId();
			int codeId = payload.getCode();
			Command cmd = m_appConfigManager.getRawCommands().get(id);

			if (cmd != null) {
				Code code = cmd.getCodes().get(codeId);

				model.setCode(code);
				model.setUpdateCommand(cmd);
			}
			break;
		case APP_CODE_SUBMIT:
			try {
				id = payload.getId();
				String codeStr = payload.getContent();
				List<String> strs = Splitters.by(":").split(codeStr);
				codeId = Integer.parseInt(strs.get(0));
				name = strs.get(1);
				int status = Integer.parseInt(strs.get(2));
				Code code = new Code(codeId);
				code.setName(name).setStatus(status);
				m_appConfigManager.updateCode(id, code);
				buildListInfo(model, payload);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_CODE_ADD:
			id = payload.getId();

			model.setId(String.valueOf(id));
			break;
		case APP_CODE_DELETE:
			try {
				id = payload.getId();
				codeId = payload.getCode();

				m_appConfigManager.deleteCode(id, codeId);
				buildListInfo(model, payload);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_SPEED_UPDATE:
		case APP_SPEED_ADD:
			id = payload.getId();
			Speed speed = m_appSpeedConfigManager.getConfig().getSpeeds().get(id);

			if (speed != null) {
				model.setSpeed(speed);
			}
			break;
		case APP_SPEED_DELETE:
			try {
				id = payload.getId();

				m_appSpeedConfigManager.deleteSpeed(id);
				buildListInfo(model, payload);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_SPEED_SUBMIT:
			try {
				id = payload.getId();
				String speedStr = payload.getContent();
				List<String> strs = Splitters.by(":").split(speedStr);
				String page = strs.get(0);
				int step = Integer.parseInt(strs.get(1));
				title = strs.get(2);
				int threshold = Integer.parseInt(strs.get(3));
				int speedId = id > 0 ? id : m_appSpeedConfigManager.generateId();
				speed = new Speed(speedId);

				speed.setPage(page).setStep(step).setTitle(title).setThreshold(threshold);
				m_appSpeedConfigManager.updateConfig(speed);
				buildListInfo(model, payload);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_CONFIG_UPDATE:
			String appConfig = payload.getContent();
			if (!StringUtils.isEmpty(appConfig)) {
				model.setOpState(m_appConfigManager.insert(appConfig));
			}
			model.setContent(m_appConfigManager.getConfig().toString());
			break;
		case APP_RULE:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_RULE_ADD_OR_UPDATE:
			buildAppConfigInfo(m_appConfigManager, model);
			generateRuleConfigContent(payload.getRuleId(), m_appRuleConfigManager, model);
			break;
		case APP_RULE_ADD_OR_UPDATE_SUBMIT:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setOpState(addSubmitRule(m_appRuleConfigManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_RULE_DELETE:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setOpState(deleteRule(m_appRuleConfigManager, payload.getRuleId()));
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_COMPARISON_CONFIG_UPDATE:
			String appComparisonConfig = payload.getContent();
			if (!StringUtils.isEmpty(appComparisonConfig)) {
				model.setOpState(m_appComparisonConfigManager.insert(appComparisonConfig));
			}
			model.setContent(m_appComparisonConfigManager.getConfig().toString());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private void buildListInfo(Model model, Payload payload) {
		List<Command> commands = m_appConfigManager.queryCommands();
		model.setCommands(commands);
		model.setSpeeds(m_appSpeedConfigManager.getConfig().getSpeeds());

		int id = 1;
		if ("code".equals(payload.getType()) && payload.getId() > 0) {
			id = payload.getId();
		}
		model.setCodes(m_appConfigManager.getCodes());
		Command cmd = m_appConfigManager.getRawCommands().get(id);

		if (cmd != null) {
			model.setUpdateCommand(cmd);
			model.setId(String.valueOf(id));
		}
	}
}
