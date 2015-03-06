package com.dianping.cat.report.alert.summary.build;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;
import com.dianping.cat.home.alert.summary.transform.BaseVisitor;
import com.dianping.cat.report.alert.AlertType;

public class AlertSummaryVisitor extends BaseVisitor {

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private Map<Object, Object> m_categoryMap = new LinkedHashMap<Object, Object>();

	private List<Map<Object, Object>> m_alertList;

	private DateFormat m_fmt = new SimpleDateFormat("HH:mm");

	private String m_domain;

	public static final String LONG_CALL_NAME = "超时依赖调用";

	public AlertSummaryVisitor(String domain) {
		m_domain = domain;
	}

	private String convertNameToChinese(String name) {
		if (name.equals(AlertType.Network.getName())) {
			return "网络告警";
		}
		if (name.equals(AlertType.Business.getName())) {
			return "业务告警";
		}
		if (name.equals(AlertType.Exception.getName())) {
			return "异常告警";
		}
		if (name.equals(AlertInfoBuilder.LONG_CALL)) {
			return LONG_CALL_NAME;
		}
		if (name.equals(AlertInfoBuilder.PREFIX + AlertType.Exception.getName())) {
			return "依赖异常告警";
		}
		if (name.equals(AlertType.System.getName())) {
			return "系统告警";
		}

		return "";
	}

	public Map<Object, Object> getResult() {
		return m_result;
	}

	@Override
	public void visitAlert(Alert alert) {
		Map<Object, Object> tmpALertMap = new HashMap<Object, Object>();

		String alertDomain = alert.getDomain();
		if (alertDomain != null && alertDomain.equals(m_domain)) {
			tmpALertMap.put("metric", alert.getMetric());
		} else {
			tmpALertMap.put("metric", alertDomain + "<br>" + alert.getMetric());
		}
		tmpALertMap.put("domain", alert.getDomain());
		tmpALertMap.put("dateStr", m_fmt.format(alert.getAlertTime()));
		tmpALertMap.put("type", alert.getType());
		tmpALertMap.put("context", alert.getContext());
		tmpALertMap.put("count", alert.getCount());

		m_alertList.add(tmpALertMap);
	}

	@Override
	public void visitAlertSummary(AlertSummary alertSummary) {
		Date date = alertSummary.getAlertDate();
		m_result.put("domain", alertSummary.getDomain());
		m_result.put("dateStr", m_fmt.format(date));
		m_result.put("categories", m_categoryMap);

		for (Category category : alertSummary.getCategories().values()) {
			visitCategory(category);
		}
	}

	@Override
	public void visitCategory(Category category) {
		m_alertList = new ArrayList<Map<Object, Object>>();

		for (Alert alert : category.getAlerts()) {
			visitAlert(alert);
		}

		m_categoryMap.put(convertNameToChinese(category.getName()), m_alertList);
	}
}
