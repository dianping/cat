package com.dianping.cat.report.page.cdn;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("province")
	private String m_province = "ALL";

	@FieldMeta("city")
	private String m_city = "ALL";

	@FieldMeta("cdn")
	private String m_cdn = "ALL";

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.CDN);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getCdn() {
		return m_cdn;
	}

	public String getCity() {
		return m_city;
	}

	public Date getHistoryEndDate() {
		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				return m_format.parse(m_customEnd);
			} else {
				return TimeHelper.getCurrentHour(1);
			}
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(1);
		}
	}

	public Date getHistoryStartDate() {
		try {
			if (m_customStart != null && m_customStart.length() > 0) {

				return m_format.parse(m_customStart);
			} else {
				return TimeHelper.getCurrentHour(-2);
			}
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(-2);
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getProvince() {
		return m_province;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setCdn(String cdn) {
		m_cdn = cdn;
	}

	public void setCity(String city) {
		m_city = city;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.CDN);
	}

	public void setProvince(String province) {
		m_province = province;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
