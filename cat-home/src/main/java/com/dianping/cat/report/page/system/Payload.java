package com.dianping.cat.report.page.system;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain = Constants.CAT;

	@FieldMeta("productLine")
	private String m_productLine = "All";

	@FieldMeta("type")
	private String m_type = "paasSystem";

	@FieldMeta("ipAddrs")
	private String m_ipAddrs = "All";

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.SYSTEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getIpAddrs() {
		return m_ipAddrs;
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

	public String getProductLine() {
		return m_productLine;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.SYSTEM);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.SYSTEM);
	}

	public void setProductLine(String productLine) {
		m_productLine = productLine;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddrs(String ipAddrs) {
		m_ipAddrs = ipAddrs;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.SYSTEM;
		}
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}
}
