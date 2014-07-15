package com.dianping.cat.report.task.alert.summary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;
import com.dianping.cat.home.alert.summary.transform.BaseVisitor;

public class AlertSummaryVisitor extends BaseVisitor {

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private Map<Object, Object> m_categoryMap = new HashMap<Object, Object>();

	private List<Map<Object, Object>> m_alertList = new ArrayList<Map<Object, Object>>();

	private DateFormat m_fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public Map<Object, Object> getResult() {
		return m_result;
	}

	@Override
	public void visitAlert(Alert alert) {
		Map<Object, Object> tmpALertMap = new HashMap<Object, Object>();

		tmpALertMap.put("dateStr", m_fmt.format(alert.getAlertTime()));
		tmpALertMap.put("domain", alert.getDomain());
		tmpALertMap.put("metric", alert.getMetric());
		tmpALertMap.put("type", alert.getType());
		tmpALertMap.put("context", alert.getContext());

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
		for (Alert alert : category.getAlerts()) {
			visitAlert(alert);
		}

		m_result.put(category.getName(), m_alertList);
		m_alertList = new ArrayList<Map<Object, Object>>();
	}
}
