package com.dianping.cat.report.page.alert;

import java.util.Date;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.alert.Handler.AlertMinute;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private String m_alertResult;

	private Map<String, AlertMinute> m_alertMinutes;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, AlertMinute> getAlertMinutes() {
		return m_alertMinutes;
	}

	public String getAlertResult() {
		return m_alertResult;
	}

	public Date getDate() {
		return new Date();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return Constants.CAT;
	}

	public String getIpAddress() {
		return null;
	}

	public void setAlertMinutes(Map<String, AlertMinute> alertMinutes) {
		m_alertMinutes = alertMinutes;
	}

	public void setAlertResult(String alertResult) {
		m_alertResult = alertResult;
	}

}
