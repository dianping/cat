package com.dianping.cat.report.page.externalError;

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

	@FieldMeta("recevice")
	private String m_recevice;

	@FieldMeta("database")
	private String m_database;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("time")
	private String m_time;

	@FieldMeta("link")
	private String m_link;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getContent() {
		return m_content;
	}

	public String getDatabase() {
		return m_database;
	}

	public String getIp() {
		return m_ip;
	}

	public String getLink() {
		return m_link;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getRecevice() {
		return m_recevice;
	}

	public String getTime() {
		return m_time;
	}

	public String getTitle() {
		return m_title;
	}

	public int getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setLink(String link) {
		m_link = link;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.EXTERNALERROR);
	}

	public void setRecevice(String recevice) {
		m_recevice = recevice;
	}

	public void setTime(String time) {
		m_time = time;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setType(int type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
