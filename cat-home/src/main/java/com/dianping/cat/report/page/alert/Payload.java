package com.dianping.cat.report.page.alert;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.site.lookup.util.StringUtils;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("channel")
	private String m_channel;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("receivers")
	private String m_receivers;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getChannel() {
		if (StringUtils.isEmpty(m_channel)) {
			return "";
		} else {
			return m_channel;
		}
	}

	public String getContent() {
		if (StringUtils.isEmpty(m_content)) {
			return "";
		} else {
			return m_content;
		}
	}

	public String getGroup() {
		if (StringUtils.isEmpty(m_group)) {
			return "default";
		} else {
			return m_group;
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getReceivers() {
		if (StringUtils.isEmpty(m_receivers)) {
			return "";
		} else {
			return m_receivers;
		}
	}

	public String getTitle() {
		if (StringUtils.isEmpty(m_title)) {
			return "";
		} else {
			return m_title;
		}
	}

	public String getType() {
		if (StringUtils.isEmpty(m_type)) {
			return "call";
		} else {
			return m_type;
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.ALERT);
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ALERT);
	}

	public void setReceivers(String receivers) {
		m_receivers = receivers;
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
			m_action = Action.ALERT;
		}
	}
}
