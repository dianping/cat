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
		AlertSummaryVisitor visitor = new AlertSummaryVisitor();
		visitor.visitAlertSummary(alertSummary);

		Map<Object, Object> dataMap = convertDataMap(visitor.getResult());
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("summary.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

	private Map<Object, Object> convertDataMap(Map<Object, Object> map) {
		return gatherDomainsForDependBusiness(map);
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> gatherDomainsForDependBusiness(Map<Object, Object> map) {
		try {
			Map<Object, Object> categories = (Map<Object, Object>) map.get("categories");
			List<Map<Object, Object>> alerts = (List<Map<Object, Object>>) categories.get("dependency_business");
			Map<String, List<Map<Object, Object>>> dependBusiMap = new TreeMap<String, List<Map<Object, Object>>>();

			for (Map<Object, Object> alert : alerts) {
				String domain = (String) alert.get("domain");
				List<Map<Object, Object>> tmpAlerts = dependBusiMap.get(domain);

				if (tmpAlerts == null) {
					tmpAlerts = new ArrayList<Map<Object, Object>>();
					dependBusiMap.put(domain, tmpAlerts);
				}
				tmpAlerts.add(alert);
			}

			categories.put("dependency_business_length", alerts.size());
			categories.put("dependency_business", dependBusiMap);
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
