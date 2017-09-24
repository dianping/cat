package com.dianping.cat.report.alert;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class RuleFTLDecorator implements Initializable {

	public Configuration m_configuration;

	public String generateConfigsHtml(String templateValue) {
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);

		dataMap.put("configs", templateValue);
		try {
			Template configsTemplate = m_configuration.getTemplate("rule_configs.ftl");
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
