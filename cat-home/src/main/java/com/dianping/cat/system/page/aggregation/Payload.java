package com.dianping.cat.system.page.aggregation;

import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("id")
	private int m_id;
	
	@FieldMeta("op")
	private Action m_action;
   
	@FieldMeta("type")
	private int m_type;
   
	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("pattern")
	private String m_pattern;

	@FieldMeta("display_name")
	private String m_displayName;

	@FieldMeta("sample")
	private String m_sample;

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.ALL);
	}
	public String getReportType() {
		return "";
	}
	
	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}
	
	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.AGGREGATION);
	}
	
	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.ALL;
		}
	}
	public int getId() {
		return m_id;
	}
	public int getType() {
		return m_type;
	}
	public String getDomain() {
		return m_domain;
	}
	public String getPattern() {
		return m_pattern;
	}
	public String getDisplayName() {
		return m_displayName;
	}
	public String getSample() {
		return m_sample;
	}
	public void setPage(SystemPage page) {
		m_page = page;
	}
	public void setId(int id) {
		m_id = id;
	}
	public void setType(int type) {
		m_type = type;
	}
	public void setDomain(String domain) {
		m_domain = domain;
	}
	public void setPattern(String pattern) {
		m_pattern = pattern;
	}
	public void setDisplayName(String displayName) {
		m_displayName = displayName;
	}
	public void setSample(String sample) {
		m_sample = sample;
	}
}
