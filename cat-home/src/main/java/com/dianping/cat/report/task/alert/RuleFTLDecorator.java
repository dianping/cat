package com.dianping.cat.report.task.alert;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class RuleFTLDecorator implements Initializable {

	public Configuration m_configuration;

	public String generateMetricItemsHtml(String metricsStr, String metricTemplateName) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);
		metricsStr = metricsStr.replaceAll("\n", "").replace("\r", "");

		dataMap.put("metricItems", metricsStr);
		try {
			Template metricsTemplate = m_configuration.getTemplate(metricTemplateName);
			metricsTemplate.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

	public String generateConfigsHtml(String link, String configsStr, String configTemplateName) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);
		configsStr = configsStr.replaceAll("\n", "").replace("\r", "");

		dataMap.put("link", link);
		dataMap.put("configs", configsStr);
		try {
			Template configsTemplate = m_configuration.getTemplate(configTemplateName);
			configsTemplate.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
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
