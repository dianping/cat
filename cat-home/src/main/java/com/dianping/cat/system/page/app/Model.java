package com.dianping.cat.system.page.app;

import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.alarm.service.AppAlarmRuleInfo;
import com.dianping.cat.command.entity.Code;
import com.dianping.cat.command.entity.Codes;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.AppCommandConfigManager.AppCommandInfo;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.configuration.group.entity.AppCommandGroupConfig;
import com.dianping.cat.configuration.mobile.entity.ConstantItem;
import com.dianping.cat.configuration.mobile.entity.Item;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.system.SystemPage;

@ModelMeta("model")
public class Model extends ViewModel<SystemPage, Action, Context> {

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_versions;

	private Map<Integer, Item> m_connectionTypes;

	private Map<Integer, Speed> m_speeds;

	private Map<String, Codes> m_codes;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_platforms;

	private Map<Integer, Item> m_apps;

	private String m_id;

	private String m_domain;

	private Map<Integer, Command> m_commands;

	private transient AppCommandConfigManager m_appConfigManager;

	private transient MobileConfigManager m_mobileConfigManager;

	private String m_nameUniqueResult;

	private Command m_updateCommand;

	private List<String> m_validatePaths;

	private List<String> m_invalidatePaths;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private Code m_code;

	private Speed m_speed;

	private String m_content;

	private Map<String, List<AppAlarmRuleInfo>> m_ruleInfos;

	private AppAlarmRuleInfo m_ruleInfo;

	private Item m_appItem;

	private String m_configHeader;

	private transient AppCommandGroupConfig m_commandGroupConfig;

	private Map<Integer, List<Code>> m_command2Codes;

	private Map<String, Codes> m_globalCodes;

	private Map<String, Command> m_command2Id;

	private List<ExceptionLimit> m_crashLimits;

	private ExceptionLimit m_crashRule;

	private List<String> m_subCommands;

	public Model(Context ctx) {
		super(ctx);
		try {
			m_appConfigManager = ContainerLoader.getDefaultContainer().lookup(AppCommandConfigManager.class);
			m_mobileConfigManager = ContainerLoader.getDefaultContainer().lookup(MobileConfigManager.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public Map<String, AppCommandInfo> getApiCommands() {
		return m_appConfigManager.buildNamespace2Commands();
	}

	public Item getAppItem() {
		return m_appItem;
	}

	public Map<Integer, Item> getApps() {
		return m_apps;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public Code getCode() {
		return m_code;
	}

	public Map<String, Codes> getCodes() {
		return m_codes;
	}

	public String getCodesJson() {
		return new JsonBuilder().toJson(m_codes);
	}

	public Map<Integer, List<Code>> getCommand2Codes() {
		return m_command2Codes;
	}

	public String getCommand2CodesJson() {
		return new JsonBuilder().toJson(m_command2Codes);
	}

	public String getCommand2IdJson() {
		return new JsonBuilder().toJson(m_command2Id);
	}

	public AppCommandGroupConfig getCommandGroupConfig() {
		return m_commandGroupConfig;
	}

	public String getCommandJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryCommand2Codes());
	}

	public Map<Integer, Command> getCommands() {
		return m_commands;
	}

	public String getCommandsJson() {
		return new JsonBuilder().toJson(m_commands);
	}

	public String getConfigHeader() {
		return m_configHeader;
	}

	public Map<String, ConstantItem> getConfigItems() {
		return m_mobileConfigManager.getConfig().getConstantItems();
	}

	public Map<Integer, Item> getConnectionTypes() {
		return m_connectionTypes;
	}

	public String getContent() {
		return m_content;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.APP_LIST;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getDomain2CommandsJson() {
		return new JsonBuilder().toJson(m_appConfigManager.buildNamespace2Commands());
	}

	public String getGlobalCodesJson() {
		return new JsonBuilder().toJson(m_globalCodes);
	}

	public String getId() {
		return m_id;
	}

	public List<String> getInvalidatePaths() {
		return m_invalidatePaths;
	}

	public String getIpAddress() {
		return "";
	}

	public Map<String, List<Command>> getNamespace2Commands() {
		return m_appConfigManager.queryNamespace2Commands();
	}

	public String getNamespace2CommandsJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryNamespace2Commands());
	}

	public String getNameUniqueResult() {
		return m_nameUniqueResult;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public String getOpState() {
		return m_opState;
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public String getReportType() {
		return "";
	}

	public AppAlarmRuleInfo getRuleInfo() {
		return m_ruleInfo;
	}

	public Map<String, List<AppAlarmRuleInfo>> getRuleInfos() {
		return m_ruleInfos;
	}

	public Speed getSpeed() {
		return m_speed;
	}

	public Map<Integer, Speed> getSpeeds() {
		return m_speeds;
	}

	public List<String> getSubCommands() {
		return m_subCommands;
	}

	public String getSubCommandsJson() {
		return new JsonBuilder().toJson(m_subCommands);
	}

	public Command getUpdateCommand() {
		return m_updateCommand;
	}

	public List<String> getValidatePaths() {
		return m_validatePaths;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public List<ExceptionLimit> getCrashLimits() {
		return m_crashLimits;
	}

	public ExceptionLimit getCrashRule() {
		return m_crashRule;
	}

	public void setCrashRule(ExceptionLimit crashRule) {
		m_crashRule = crashRule;
	}

	public void setCrashLimits(List<ExceptionLimit> crashLimits) {
		m_crashLimits = crashLimits;
	}

	public void setAppItem(Item appItem) {
		m_appItem = appItem;
	}

	public void setApps(Map<Integer, Item> apps) {
		m_apps = apps;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCode(Code code) {
		m_code = code;
	}

	public void setCodes(Map<String, Codes> codes) {
		m_codes = codes;
	}

	public void setCommand2Codes(Map<Integer, List<Code>> command2Codes) {
		m_command2Codes = command2Codes;
	}

	public void setCommand2Id(Map<String, Command> command2Id) {
		m_command2Id = command2Id;
	}

	public void setCommandGroupConfig(AppCommandGroupConfig commandGroupConfig) {
		m_commandGroupConfig = commandGroupConfig;
	}

	public void setCommands(Map<Integer, Command> map) {
		m_commands = map;
	}

	public void setConfigHeader(String configHeader) {
		m_configHeader = configHeader;
	}

	public void setConnectionTypes(Map<Integer, Item> connectionTypes) {
		m_connectionTypes = connectionTypes;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setGlobalCodes(Map<String, Codes> globalCodeses) {
		m_globalCodes = globalCodeses;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setInvalidatePaths(List<String> invalidatePaths) {
		m_invalidatePaths = invalidatePaths;
	}

	public void setNameUniqueResult(String nameUniqueResult) {
		m_nameUniqueResult = nameUniqueResult;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setRuleInfo(AppAlarmRuleInfo ruleInfo) {
		m_ruleInfo = ruleInfo;
	}

	public void setRuleInfos(Map<String, List<AppAlarmRuleInfo>> ruleInfos) {
		m_ruleInfos = ruleInfos;
	}

	public void setSpeed(Speed speed) {
		m_speed = speed;
	}

	public void setSpeeds(Map<Integer, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setSubCommands(List<String> subCommands) {
		m_subCommands = subCommands;
	}

	public void setUpdateCommand(Command updateCommand) {
		m_updateCommand = updateCommand;
	}

	public void setValidatePaths(List<String> validatePaths) {
		m_validatePaths = validatePaths;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}
}
