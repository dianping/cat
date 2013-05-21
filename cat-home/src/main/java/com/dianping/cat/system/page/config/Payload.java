package com.dianping.cat.system.page.config;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.home.dal.report.AggregationRule;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("department")
	private String m_department;

	@FieldMeta("email")
	private String m_email;

	@FieldMeta("owner")
	private String m_owner;

	private SystemPage m_page;

	@FieldMeta("projectId")
	private int m_projectId;

	@FieldMeta("projectLine")
	private String m_projectLine;

	@FieldMeta("domain")
	private String m_domain;
	
	@ObjectMeta("aggregation")
	private AggregationRule m_rule = new AggregationRule();
	
	@FieldMeta("id")
	private int m_id;
   
	@FieldMeta("type")
	private int m_type;

	@FieldMeta("pattern")
	private String m_pattern;

	@FieldMeta("display_name")
	private String m_displayName;

	@FieldMeta("sample")
	private String m_sample;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.PROJECT_ALL;
		}
		return m_action;
	}

	public String getDepartment() {
		return m_department;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getEmail() {
		return m_email;
	}

	public String getOwner() {
		return m_owner;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public int getProjectId() {
		return m_projectId;
	}

	public String getProjectLine() {
		return m_projectLine;
	}

	public String getReportType() {
		return "";
	}

	public void setAction(String action) {
		m_action =Action.getByName(action, Action.PROJECT_ALL);
	}

	public void setDepartment(String department) {
		m_department = department;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setEmail(String email) {
		m_email = email;
	}

	public void setOwner(String owner) {
		m_owner = owner;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.CONFIG);
	}

	public void setProjectId(int projectId) {
		m_projectId = projectId;
	}

	public void setProjectLine(String projectLine) {
		m_projectLine = projectLine;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}

	public int getId() {
   	return m_id;
   }

	public void setId(int id) {
   	m_id = id;
   }

	public int getType() {
   	return m_type;
   }

	public void setType(int type) {
   	m_type = type;
   }

	public String getPattern() {
   	return m_pattern;
   }

	public void setPattern(String pattern) {
   	m_pattern = pattern;
   }

	public String getDisplayName() {
   	return m_displayName;
   }

	public void setDisplayName(String displayName) {
   	m_displayName = displayName;
   }

	public String getSample() {
   	return m_sample;
   }

	public void setSample(String sample) {
   	m_sample = sample;
   }

	public AggregationRule getRule() {
   	return m_rule;
   }

	public void setRule(AggregationRule rule) {
   	m_rule = rule;
   }
	
}
