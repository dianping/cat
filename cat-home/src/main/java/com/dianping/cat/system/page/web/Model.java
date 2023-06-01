package com.dianping.cat.system.page.web;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.config.Level;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.configuration.web.entity.ConfigItem;
import com.dianping.cat.configuration.web.entity.Item;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@ModelMeta("model")
public class Model extends ViewModel<SystemPage, Action, Context> {

	private Map<String, Speed> m_speeds;

	private Speed m_speed;

	private Step m_step;

	private List<ExceptionLimit> m_jsRules;

	private ExceptionLimit m_jsRule;

	private List<String> m_modules;

	private Map<Integer, Item> m_webCities;

	private Map<Integer, Item> m_webOperators;

	private Map<Integer, Item> m_webNetworks;

	private PatternItem m_patternItem;

	private Map<Integer, PatternItem> m_patternItems;

	private Map<Integer, Code> m_webCodes;

	private Code m_code;

	private Collection<Rule> m_rules;

	private String m_content;

	private String m_opState = SUCCESS;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_id;

	private String m_configHeader;

	private transient WebConfigManager m_webConfigManager;

	public Model(Context ctx) {
		super(ctx);
		try {
			m_webConfigManager = ContainerLoader.getDefaultContainer().lookup(WebConfigManager.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public Code getCode() {
		return m_code;
	}

	public String getConfigHeader() {
		return m_configHeader;
	}

	public String getContent() {
		return m_content;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.SPEED_LIST;
	}

	public String getDomain() {
		return "";
	}

	public Map<String, ConfigItem> getWebConfigItems() {
		return m_webConfigManager.getConfig().getConfigItems();
	}

	public String getId() {
		return m_id;
	}

	public String getIpAddress() {
		return "";
	}

	public ExceptionLimit getJsRule() {
		return m_jsRule;
	}

	public List<ExceptionLimit> getJsRules() {
		return m_jsRules;
	}

	public List<String> getLevels() {
		return Level.getLevels();
	}

	public List<String> getModules() {
		return m_modules;
	}

	public String getOpState() {
		return m_opState;
	}

	public PatternItem getPatternItem() {
		return m_patternItem;
	}

	public Map<Integer, PatternItem> getPatternItems() {
		return m_patternItems;
	}

	public Collection<Rule> getRules() {
		return m_rules;
	}

	public Map<String, Speed> getSpeeds() {
		return m_speeds;
	}

	public Speed getSpeed() {
		return m_speed;
	}

	public Step getStep() {
		return m_step;
	}

	public Map<Integer, Item> getWebCities() {
		return m_webCities;
	}

	public Map<Integer, Code> getWebCodes() {
		return m_webCodes;
	}

	public String getWebCodesJson() {
		return new JsonBuilder().toJson(m_webCodes);
	}

	public Map<Integer, Item> getWebNetworks() {
		return m_webNetworks;
	}

	public Map<Integer, Item> getWebOperators() {
		return m_webOperators;
	}

	public void setCode(Code code) {
		m_code = code;
	}

	public void setConfigHeader(String configHeader) {
		m_configHeader = configHeader;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setJsRule(ExceptionLimit jsRule) {
		m_jsRule = jsRule;
	}

	public void setJsRules(List<ExceptionLimit> jsRules) {
		m_jsRules = jsRules;
	}

	public void setModules(List<String> modules) {
		m_modules = modules;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setPatternItem(PatternItem patternItem) {
		m_patternItem = patternItem;
	}

	public void setPatternItems(Map<Integer, PatternItem> patternItems) {
		m_patternItems = patternItems;
	}

	public void setRules(Collection<Rule> rules) {
		m_rules = rules;
	}

	public void setSpeeds(Map<String, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setSpeed(Speed speed) {
		m_speed = speed;
	}

	public void setStep(Step step) {
		m_step = step;
	}

	public void setWebCities(Map<Integer, Item> webCities) {
		m_webCities = webCities;
	}

	public void setWebCodes(Map<Integer, Code> webCodes) {
		m_webCodes = webCodes;
	}

	public void setWebNetworks(Map<Integer, Item> webNetworks) {
		m_webNetworks = webNetworks;
	}

	public void setWebOperators(Map<Integer, Item> webOperators) {
		m_webOperators = webOperators;
	}
}
