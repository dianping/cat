package com.dianping.cat.report.task.alert.summary;

import java.io.StringWriter;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.system.notify.ReportRenderImpl;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AlertSummaryFTLDecorator implements AlertSummaryDecorator, Initializable {

	public Configuration m_configuration;
	
	public static final String ID = "AlertSummaryFTLDecorator";

	@Override
	public String generateHtml(AlertSummary alertSummary) {
		AlertSummaryVisitor visitor = new AlertSummaryVisitor();
		visitor.visitAlertSummary(alertSummary);

		Map<Object, Object> dataMap = visitor.getResult();
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("summary.ftl");
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
			m_configuration.setClassForTemplateLoading(ReportRenderImpl.class, "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
