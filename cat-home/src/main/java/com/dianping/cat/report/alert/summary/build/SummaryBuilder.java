package com.dianping.cat.report.alert.summary.build;

import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;

import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class SummaryBuilder implements Initializable {

	public Configuration m_configuration;

	protected abstract Map<Object, Object> generateModel(String domain, Date date);

	public String generateHtml(String domain, Date date) {
		Map<Object, Object> dataMap = generateModel(domain, date);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate(getTemplateAddress());
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

	protected abstract String getTemplateAddress();

	public abstract String getID();
}
