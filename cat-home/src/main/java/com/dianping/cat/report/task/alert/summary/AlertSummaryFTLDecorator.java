package com.dianping.cat.report.task.alert.summary;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.system.notify.ReportRenderImpl;

import freemarker.template.Configuration;

public class AlertSummaryFTLDecorator implements AlertSummaryDecorator, Initializable {

	public Configuration m_configuration;

	@Override
	public String generateHtml(AlertSummary alertSummary) {

		return null;
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
