package com.dianping.cat.report.alert.summary.build;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.report.alert.summary.AlertSummaryService;

public class RelatedSummaryBuilder extends SummaryBuilder {

	@Inject
	private AlertInfoBuilder m_alertSummaryManager;

	@Inject
	private AlertSummaryService m_alertSummaryService;

	public static final String ID = "AlertSummaryContentGenerator";

	@SuppressWarnings("unchecked")
	private Map<Object, Object> gatherDomainsForDependBusiness(Map<Object, Object> map) {
		try {
			Map<Object, Object> categories = (Map<Object, Object>) map.get("categories");
			List<Map<Object, Object>> alerts = (List<Map<Object, Object>>) categories
			      .get(AlertSummaryVisitor.LONG_CALL_NAME);
			Map<String, List<Map<Object, Object>>> longCallMap = new TreeMap<String, List<Map<Object, Object>>>();

			for (Map<Object, Object> alert : alerts) {
				String domain = (String) alert.get("domain");
				List<Map<Object, Object>> tmpAlerts = longCallMap.get(domain);

				if (tmpAlerts == null) {
					tmpAlerts = new ArrayList<Map<Object, Object>>();
					longCallMap.put(domain, tmpAlerts);
				}
				tmpAlerts.add(alert);
			}

			categories.remove(AlertSummaryVisitor.LONG_CALL_NAME);
			categories.put(AlertInfoBuilder.LONG_CALL, longCallMap);
			map.put(AlertInfoBuilder.LONG_CALL + "_length", alerts.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return map;
	}

	@Override
	public Map<Object, Object> generateModel(String domain, Date date) {
		AlertSummary alertSummary = m_alertSummaryManager.generateAlertSummary(domain, date);
		AlertSummaryVisitor visitor = new AlertSummaryVisitor(alertSummary.getDomain());
		
		visitor.visitAlertSummary(alertSummary);

		return gatherDomainsForDependBusiness(visitor.getResult());
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected String getTemplateAddress() {
		return "summary.ftl";
	}

}
