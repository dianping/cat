package com.dianping.cat.system.page.web;

import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@ObjectMeta("patternItem")
	private PatternItem m_patternItem = new PatternItem();

	@ObjectMeta("code")
	private Code m_code = new Code();

	@FieldMeta("page")
	private String m_webPage;

	@FieldMeta("stepId")
	private int m_stepId;

	@ObjectMeta("step")
	private Step m_step;

	@ObjectMeta("jsRule")
	private ExceptionLimit m_jsRule = new ExceptionLimit();

	@FieldMeta("ruleId")
	private String m_ruleId;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("id")
	private int m_id;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("configs")
	private String m_configs;

	@Override
	public Action getAction() {
		return m_action;
	}

	public Code getCode() {
		return m_code;
	}

	public String getConfigs() {
		return m_configs;
	}

	public String getContent() {
		return m_content;
	}

	public int getId() {
		return m_id;
	}

	public ExceptionLimit getJsRule() {
		return m_jsRule;
	}

	public String getKey() {
		return m_key;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public PatternItem getPatternItem() {
		return m_patternItem;
	}

	public String getReportType() {
		return "";
	}

	public String getRuleId() {
		return m_ruleId;
	}

	public Step getStep() {
		return m_step;
	}

	public int getStepId() {
		return m_stepId;
	}

	public String getWebPage() {
		return m_webPage;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.SPEED_LIST);
	}

	public void setCode(Code code) {
		m_code = code;
	}

	public void setConfigs(String configs) {
		m_configs = configs;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setJsRule(ExceptionLimit jsRule) {
		m_jsRule = jsRule;
	}

	public void setKey(String key) {
		m_key = key;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.WEB);
	}

	public void setPatternItem(PatternItem patternItem) {
		m_patternItem = patternItem;
	}

	public void setRuleId(String ruleId) {
		m_ruleId = ruleId;
	}

	public void setStep(Step step) {
		m_step = step;
	}

	public void setStepId(int stepId) {
		m_stepId = stepId;
	}

	public void setWebPage(String webPage) {
		m_webPage = webPage;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.SPEED_LIST;
		}
	}
}
