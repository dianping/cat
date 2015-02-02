package com.dianping.cat.report.task.notify.render;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.notify.AppDataComparisonResult;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AppDataComparisonRender implements Initializable {

	private Configuration m_configuration;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

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

	public String renderReport(Date yesterday, List<AppDataComparisonResult> results) {
		Map<Object, Object> root = new HashMap<Object, Object>();
		root.put("results", results);
		root.put("yesterday", m_sdf.format(yesterday));
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("appDataComparison.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}
}
