package com.dianping.cat.report.page.zabbixError;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private int m_type;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("database")
	private String m_database;
	
	@FieldMeta("name")
	private String m_name;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("time")
	private String m_time;
	
	public int getType() {
   	return m_type;
   }

	public void setType(int type) {
   	m_type = type;
   }

	public String getIp() {
   	return m_ip;
   }

	public void setIp(String ip) {
   	m_ip = ip;
   }

	public String getDatabase() {
   	return m_database;
   }

	public void setDatabase(String database) {
   	m_database = database;
   }

	public String getName() {
   	return m_name;
   }

	public void setName(String name) {
   	m_name = name;
   }

	public String getContent() {
   	return m_content;
   }

	public void setContent(String content) {
   	m_content = content;
   }

	public String getTime() {
   	return m_time;
   }

	public void setTime(String time) {
   	m_time = time;
   }

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ZABBIXERROR);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
