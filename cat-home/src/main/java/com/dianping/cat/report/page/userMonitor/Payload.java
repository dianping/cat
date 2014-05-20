package com.dianping.cat.report.page.userMonitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Monitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("city")
	private String m_city = "上海市";

	@FieldMeta("channel")
	private String m_channel;

	@FieldMeta("type")
	private String m_type = Monitor.TYPE_INFO;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.USERMONITOR);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getChannel() {
		return m_channel;
	}

	public String getCity() {
		return m_city;
	}

	public Date getHistoryEndDate() {
		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				return m_format.parse(m_customEnd);
			} else {
				return TimeUtil.getCurrentHour(1);
			}
		} catch (Exception e) {
			return TimeUtil.getCurrentHour(1);
		}
	}

	public Date getHistoryStartDate() {
		try {
			if (m_customStart != null && m_customStart.length() > 0) {

				return m_format.parse(m_customStart);
			} else {
				return TimeUtil.getCurrentHour(-2);
			}
		} catch (Exception e) {
			return TimeUtil.getCurrentHour(-2);
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getType() {
		return m_type;
	}

	public String getUrl() {
		return m_url;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setCity(String city) {
		m_city = city;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.USERMONITOR);
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
