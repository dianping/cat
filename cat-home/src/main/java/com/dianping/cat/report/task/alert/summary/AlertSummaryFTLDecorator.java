package com.dianping.cat.report.task.alert.summary;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AlertSummaryFTLDecorator implements AlertSummaryDecorator, Initializable {

	public Configuration m_configuration;

	public static final String ID = "AlertSummaryFTLDecorator";

	@Override
	public String generateHtml(AlertSummary alertSummary) {
		AlertSummaryVisitor visitor = new AlertSummaryVisitor(alertSummary.getDomain());
		visitor.visitAlertSummary(alertSummary);

		Map<Object, Object> dataMap = gatherDomainsForDependBusiness(visitor.getResult());
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("summary.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

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
			categories.put(AlertSummaryGenerator.LONG_CALL, longCallMap);
			map.put(AlertSummaryGenerator.LONG_CALL + "_length", alerts.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return map;
	}

	@Override
	public void initialize() throws InitializationException {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(this.getClass(), "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
