package com.dianping.cat.system.page.config.processor;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
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
			buildAllCommandInfos(model);

			if (StringUtils.isNotEmpty(payload.getDomain())) {
				id = payload.getId();
				model.setCodes(m_appConfigManager.getCodes());
				Command cmd = m_appConfigManager.getRawCommands().get(id);

				if (cmd != null) {
					model.setUpdateCommand(cmd);
				}
			}
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
			buildAllCommandInfos(model);
			break;
		case APP_PAGE_DELETE:
			id = payload.getId();

			if (m_appConfigManager.deleteCommand(id)) {
				model.setOpState(true);
			} else {
				model.setOpState(false);
			}
			buildAllCommandInfos(model);
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
			id = payload.getId();
			String codeStr = payload.getContent();
			List<String> strs = Splitters.by(":").split(codeStr);
			codeId = Integer.parseInt(strs.get(0));
			name = strs.get(1);
			int status = Integer.parseInt(strs.get(2));
			Code code = new Code(codeId);
			code.setName(name).setStatus(status);
			m_appConfigManager.updateCode(id, code);
			buildAllCommandInfos(model);
			break;

		case APP_CODE_ADD:
			id = payload.getId();

			model.setId(String.valueOf(id));
			buildAllCommandInfos(model);
			break;
		case APP_CODE_DELETE:
			id = payload.getId();
			codeId = payload.getCode();
			m_appConfigManager.getRawCommands().get(id).getCodes().remove(codeId);
			buildAllCommandInfos(model);
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

	private void buildAllCommandInfos(Model model) {
		List<Command> commands = m_appConfigManager.queryCommands();
		model.setCommands(commands);
	}

}
