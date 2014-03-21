package com.dianping.cat.report.page.alteration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("refresh")
	private boolean m_refresh = false;
	
	@FieldMeta("fullScreen")
	private boolean fullScreen = false;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("alterationDate")
	private String m_alterationDate;

	@FieldMeta("user")
	private String m_user;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("startTime")
	private String m_startTime;

	@FieldMeta("endTime")
	private String m_endTime;

	@FieldMeta("granularity")
	private long m_granularity;

	@FieldMeta("hostname")
	private String m_hostname;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Payload() {
		super(ReportPage.ALTERATION);
	}

	@Override
	public Action getAction() {
		if (m_action == null) {
			return Action.VIEW;
		}else{
			return m_action;
		}
	}

	public Date getAlterationDate() {
		try {
	      return m_sdf.parse(m_alterationDate);
      } catch (ParseException e) {
      	return new Date();
      }
	}

	public String getContent() {
		return m_content;
	}

	public String getDomain() {
		if ("".equals(m_domain)) {
			return null;
		} else {
			return m_domain;
		}
	}

	public Date getEndTime() {
		if (m_endTime == null || m_endTime.length() == 0) {
			return new Date();
		} else {
			try {
				return m_sdf.parse(m_endTime);
			} catch (ParseException e) {
				return new Date();
			}
		}
	}

	public int getFrequency() {
		return m_frequency;
	}

	public long getGranularity() {
		return m_granularity;
	}

	public String getHostname() {
		if("".equals(m_hostname)){
			return null;
		}else{
			return m_hostname;
		}
	}

	public String getIp() {
		return m_ip;
	}

	public ReportPage getPage() {
		return m_page;
	}

	public Date getStartTime() {
		if (m_startTime == null || m_startTime.length() == 0) {
			return new Date(System.currentTimeMillis() - TimeUtil.ONE_HOUR);
		} else {
			try {
				return m_sdf.parse(m_startTime);
			} catch (ParseException e) {
				return new Date();
			}
		}
	}

	public String getTitle() {
		return m_title;
	}

	public String getType() {
		return m_type;
	}

	public String getUrl() {
		return m_url;
	}

	public String getUser() {
		return m_user;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setAlterationDate(String alterationDate) {
		m_alterationDate = alterationDate;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public void setGranularity(long granularity) {
		m_granularity = granularity;
	}

	public void setHostname(String hostname) {
		m_hostname = hostname;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ALTERATION);
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	public void setUser(String user) {
		m_user = user;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
