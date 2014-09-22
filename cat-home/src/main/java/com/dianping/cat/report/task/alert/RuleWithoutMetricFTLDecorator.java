package com.dianping.cat.report.task.alert;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class RuleWithoutMetricFTLDecorator implements Initializable {

	public Configuration m_configuration;

	public String generateRuleHtml(String link, String jsonStr) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);
		jsonStr = jsonStr.replaceAll("\n", "").replace("\r", "");

		dataMap.put("link", link);
		dataMap.put("rule", jsonStr);
		try {
			Template t = m_configuration.getTemplate("ruleWithoutMetric.ftl");
			t.process(dataMap, sw);
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
