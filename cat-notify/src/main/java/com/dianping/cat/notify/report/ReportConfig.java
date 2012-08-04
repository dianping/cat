package com.dianping.cat.notify.report;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.notify.model.entity.Report;
import com.dianping.cat.notify.model.entity.Template;
import com.dianping.cat.notify.model.entity.TemplateReport;

public class ReportConfig {
	private String m_createClass;

	private String m_schedule;

	private Map<String, Template> m_templates = new LinkedHashMap<String, Template>();
	
	public ReportConfig(Report report){
		this.m_createClass = report.getCreateClass();
		this.m_schedule = report.getSchedule();
		this.m_templates = report.getTemplates();
	}
	
    public ReportConfig(TemplateReport report){
    	this.m_createClass = report.getCreateClass();
		this.m_schedule = report.getSchedule();
		this.m_templates = report.getTemplates();
	}

	public String getCreateClass() {
		return m_createClass;
	}

	public void setCreateClass(String createClass) {
		this.m_createClass = createClass;
	}

	public String getSchedule() {
		return m_schedule;
	}

	public void setSchedule(String schedule) {
		this.m_schedule = schedule;
	}

	public Map<String, Template> getTemplates() {
		return m_templates;
	}

	public void setTemplates(Map<String, Template> templates) {
		this.m_templates = templates;
	}

}
