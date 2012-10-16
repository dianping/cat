package com.dianping.cat.system.notify;

import java.io.StringWriter;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.system.notify.render.EventRender;
import com.dianping.cat.system.notify.render.ProblemRender;
import com.dianping.cat.system.notify.render.TransactionRender;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ReportRenderImpl implements ReportRender, Initializable {

	public Configuration m_configuration;

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

	@Override
	public String renderReport(EventReport report) {
		EventRender entity = new EventRender(report.getStartTime(), report.getDomain(), 1);
		entity.visitEventReport(report);

		Map<Object, Object> root = entity.getRenderResult();
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("event.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

	@Override
	public String renderReport(HealthReport report) {
		return "";
	}

	@Override
	public String renderReport(ProblemReport report) {
		ProblemRender entity = new ProblemRender(report.getStartTime(), report.getDomain());
		entity.visitProblemReport(report);

		Map<Object, Object> root = entity.getRenderResult();
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("problem.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

	@Override
	public String renderReport(TransactionReport report) {
		TransactionRender entity = new TransactionRender(report.getStartTime(), report.getDomain(), 1);
		entity.visitTransactionReport(report);

		Map<Object, Object> root = entity.getRenderResult();
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("transaction.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

}
