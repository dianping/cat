package com.dianping.cat.report.page.alert;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private String m_alertResult;

	private Map<String, List<Alert>> m_alerts;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getAlertResult() {
		return m_alertResult;
	}

	public Map<String, List<Alert>> getAlerts() {
		return m_alerts;
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

	public void setAlertResult(String alertResult) {
		m_alertResult = alertResult;
	}

	public void setAlerts(Map<String, List<Alert>> alerts) {
		m_alerts = alerts;
	}
}
