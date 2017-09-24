package com.dianping.cat.report.alert.sender.decorator;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ExceptionDecorator extends ProjectDecorator implements Initializable {

	@Inject
	private AlertSummaryExecutor m_executor;

	public Configuration m_configuration;

	public static final String ID = AlertType.Exception.getName();

	protected DateFormat m_linkFormat = new SimpleDateFormat("yyyyMMddHH");

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> dataMap = generateExceptionMap(alert);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("exceptionAlert.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError("build exception content error:" + alert.toString(), e);
		}

		String alertContent = sw.toString();
		String summaryContext = "";

		try {
			summaryContext = m_executor.execute(alert.getGroup(), alert.getDate());
		} catch (Exception e) {
			Cat.logError(alert.toString(), e);
		}

		if (summaryContext != null) {
			return alertContent + "<br/>" + summaryContext;
		} else {
			return alertContent;
		}
	}

	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		String domain = alert.getGroup();
		String contactInfo = buildContactInfo(domain);
		Map<Object, Object> map = new HashMap<Object, Object>();

		map.put("domain", domain);
		map.put("content", alert.getContent());
		map.put("date", m_format.format(alert.getDate()));
		map.put("linkDate", m_linkFormat.format(alert.getDate()));
		map.put("contactInfo", contactInfo);

		return map;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[CAT异常告警] [项目: ").append(alert.getGroup()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
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
