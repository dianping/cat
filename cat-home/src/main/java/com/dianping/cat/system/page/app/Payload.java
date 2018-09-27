package com.dianping.cat.system.page.app;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("id")
	private int m_id;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("threshold")
	private int m_threshold = 30;

	@FieldMeta("code")
	private int m_code;

	@FieldMeta("constant")
	private boolean m_constant = false;

	@FieldMeta("ruleId")
	private String m_ruleId;

	@FieldMeta("configs")
	private String m_configs;

	@FieldMeta("attributes")
	private String m_attributes;

	@FieldMeta("all")
	private boolean m_all;

	@FieldMeta("parent")
	private String m_parent;

	@FieldMeta("namespace")
	private String m_namespace;
	
	@ObjectMeta("crashRule")
	private ExceptionLimit m_crashRule = new ExceptionLimit();

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getAttributes() {
		return m_attributes;
	}

	public int getCode() {
		return m_code;
	}

	public String getConfigs() {
		return m_configs;
	}

	public String getContent() {
		return m_content;
	}

	public String getDomain() {
		return m_domain;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getParent() {
		return m_parent;
	}

	public String getReportType() {
		return "";
	}

	public String getRuleId() {
		return m_ruleId;
	}

	public int getThreshold() {
		return m_threshold;
	}

	public String getTitle() {
		return m_title;
	}

	public String getType() {
		return m_type;
	}
	
	public ExceptionLimit getCrashRule() {
		return m_crashRule;
	}

	public void setCrashRule(ExceptionLimit crashRule) {
		m_crashRule = crashRule;
	}

	public boolean isAll() {
		return m_all;
	}

	public boolean isConstant() {
		return m_constant;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.APP_LIST);
	}

	public void setAll(boolean all) {
		m_all = all;
	}

	public void setAttributes(String attributes) {
		m_attributes = attributes;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public void setConfigs(String configs) {
		m_configs = configs;
	}

	public void setConstant(boolean constant) {
		m_constant = constant;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setNamespace(String namespace) {
		m_namespace = namespace;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.APP);
	}

	public void setParent(String parent) {
		m_parent = parent;
	}

	public void setRuleId(String ruleId) {
		m_ruleId = ruleId;
	}

	public void setThreshold(int threshold) {
		m_threshold = threshold;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.APP_LIST;
		}
	}
}
