package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.config.app.command.CommandFormatConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class AppConfigProcessor extends BaseProcesser implements Initializable {

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppComparisonConfigManager m_appComparisonConfigManager;

	@Inject
	private EventReportService m_eventReportService;

	@Inject
	private CommandFormatConfigManager m_urlConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private Set<String> m_invalids = new HashSet<String>();

	public void appRuleBatchUpdate(Payload payload, Model model) {
		String content = payload.getContent();
		String[] paths = content.split(",");

		for (String path : paths) {
			try {
				if (StringUtils.isNotEmpty(path) && !m_appConfigManager.getCommands().containsKey(path)) {
					Command command = new Command();

					command.setDomain("").setTitle(path).setName(path);
					m_appConfigManager.addCommand(command);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private void buildAppConfigInfo(AppConfigManager appConfigManager, Model model) {
		model.setConnectionTypes(appConfigManager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(appConfigManager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(appConfigManager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(appConfigManager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(appConfigManager.queryCommands());
	}

	public void buildBatchApiConfig(Payload payload, Model model) {
		Date start = TimeHelper.getCurrentDay(-1);
		Date end = TimeHelper.getCurrentDay();
		EventReport report = m_eventReportService.queryReport(Constants.BROKER_SERVICE, start, end);
		EventReportVisitor visitor = new EventReportVisitor();

		visitor.visitEventReport(report);
		Set<String> validatePaths = visitor.getPaths();
		Set<String> invalidatePaths = visitor.getInvalidatePaths();

		Map<String, Command> commands = m_appConfigManager.getCommands();

		for (Entry<String, Command> entry : commands.entrySet()) {
			validatePaths.remove(entry.getKey());
			invalidatePaths.remove(entry.getKey());
		}

		model.setValidatePaths(new ArrayList<String>(validatePaths));
		model.setInvalidatePaths(new ArrayList<String>(invalidatePaths));
	}

	private void buildListInfo(Model model, Payload payload) {
		int id = 0;
		List<Command> commands = m_appConfigManager.queryCommands();

		if ("code".equals(payload.getType()) && payload.getId() > 0) {
			id = payload.getId();
		} else {
			if (!commands.isEmpty()) {
				id = commands.iterator().next().getId();
			}
		}
		Command cmd = m_appConfigManager.getRawCommands().get(id);

		if (cmd != null) {
			model.setUpdateCommand(cmd);
			model.setId(String.valueOf(id));
		}

		buildBatchApiConfig(payload, model);
		model.setSpeeds(m_appSpeedConfigManager.getConfig().getSpeeds());
		model.setCodes(m_appConfigManager.getCodes());
	}

	@Override
	public void initialize() throws InitializationException {
		m_invalids.add("jpg");
		m_invalids.add("http");
		m_invalids.add("file");
		m_invalids.add("zip");
		m_invalids.add("patch");
		m_invalids.add("dianping://");
		m_invalids.add("data:");
		m_invalids.add(".js");
		m_invalids.add("OTHERS");
		m_invalids.add("hit-");
	}

	public void process(Action action, Payload payload, Model model) {
		int id;

		switch (action) {
		case APP_NAME_CHECK:
			if (m_appConfigManager.isNameDuplicate(payload.getName())) {
				model.setNameUniqueResult("{\"isNameUnique\" : false}");
			} else {
				model.setNameUniqueResult("{\"isNameUnique\" : true}");
			}
			break;
		case APP_LIST:
			buildListInfo(model, payload);
			break;
		case APP_COMMMAND_UPDATE:
			id = payload.getId();

			if (m_appConfigManager.containCommand(id)) {
				Command command = m_appConfigManager.getConfig().findCommand(id);

				if (command == null) {
					command = new Command();
				}
				model.setUpdateCommand(command);
			}
			break;
		case APP_COMMAND_SUBMIT:
			id = payload.getId();
			String domain = payload.getDomain();
			String name = payload.getName();
			String title = payload.getTitle();
			boolean all = payload.isAll();
			int timeThreshold = payload.getThreshold();

			if (m_appConfigManager.containCommand(id)) {
				Command command = new Command();

				command.setDomain(domain).setName(name).setTitle(title).setAll(all).setThreshold(timeThreshold);

				if (m_appConfigManager.updateCommand(id, command)) {
					model.setOpState(true);
				} else {
					model.setOpState(false);
				}
			} else {
				try {
					Command command = new Command().setDomain(domain).setTitle(title).setName(name).setAll(all)
					      .setThreshold(timeThreshold);

					if (m_appConfigManager.addCommand(command).getKey()) {
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
		case APP_COMMAND_DELETE:
			id = payload.getId();

			if (m_appConfigManager.deleteCommand(id)) {
				m_appRuleConfigManager.deleteByCommandId(id);
				model.setOpState(true);
			} else {
				model.setOpState(false);
			}
			buildListInfo(model, payload);
			break;
		case APP_CODE_UPDATE:
			id = payload.getId();
			int codeId = payload.getCode();

			if (payload.isConstant()) {
				Code code = m_appConfigManager.getConfig().getCodes().get(codeId);

				model.setCode(code);
			} else {
				Command cmd = m_appConfigManager.getRawCommands().get(id);

				if (cmd != null) {
					Code code = cmd.getCodes().get(codeId);

					model.setCode(code);
					model.setUpdateCommand(cmd);
				}
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

				if (payload.isConstant()) {
					m_appConfigManager.updateCode(code);
				} else {
					m_appConfigManager.updateCode(id, code);
				}
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

				if (payload.isConstant()) {
					m_appConfigManager.getCodes().remove(codeId);
				} else {
					m_appConfigManager.deleteCode(id, codeId);
				}
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
				String page = strs.get(0).trim();
				int step = Integer.parseInt(strs.get(1).trim());
				title = strs.get(2).trim();
				int threshold = Integer.parseInt(strs.get(3).trim());
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
			model.setContent(m_configHtmlParser.parse(m_appConfigManager.getConfig().toString()));
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
			model.setContent(m_configHtmlParser.parse(m_appComparisonConfigManager.getConfig().toString()));
			break;
		case APP_RULE_BATCH_UPDATE:
			appRuleBatchUpdate(payload, model);
			buildListInfo(model, payload);
			break;
		case APP_CONSTANT_ADD:
			break;
		case APP_CONSTANT_UPDATE:
			Item item = m_appConfigManager.queryItem(payload.getType(), payload.getId());

			model.setAppItem(item);
			break;
		case APP_CONSTATN_SUBMIT:
			try {
				id = payload.getId();
				String content = payload.getContent();
				String[] strs = content.split(":");
				String type = strs[0];
				int constantId = Integer.valueOf(strs[1]);
				String value = strs[2];

				model.setOpState(m_appConfigManager.addConstant(type, constantId, value));
				buildListInfo(model, payload);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_COMMAND_FORMAT_CONFIG:
			String content = payload.getContent();

			if (StringUtils.isNotEmpty(content)) {
				m_urlConfigManager.insert(content);
			}
			model.setContent(m_configHtmlParser.parse(m_urlConfigManager.getUrlFormat().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	public class EventReportVisitor extends BaseVisitor {
		private Set<String> m_paths = new HashSet<String>();

		private Set<String> m_invalidatePaths = new HashSet<String>();

		public Set<String> getInvalidatePaths() {
			return m_invalidatePaths;
		}

		public Set<String> getPaths() {
			return m_paths;
		}

		private boolean invalidate(String name) {
			for (String str : m_invalids) {
				if (StringUtils.isEmpty(str) || name.indexOf(str) > -1) {
					return true;
				}
			}
			return false;
		}

		public void setInvalidatePaths(Set<String> invalidatePaths) {
			m_invalidatePaths = invalidatePaths;
		}

		@Override
		public void visitName(EventName name) {
			String id = name.getId();

			if (invalidate(id)) {
				m_invalidatePaths.add(id);
			} else {
				m_paths.add(id);
			}
		}

		@Override
		public void visitType(EventType type) {
			if (type.getId().equals("UnknownCommand")) {
				super.visitType(type);
			}
		}
	}
}
