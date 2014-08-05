package com.dianping.cat.report.task.alert.sender.decorator;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.system.notify.ReportRenderImpl;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ExceptionDecorator extends DefaultDecorator implements Initializable {

	public Configuration m_configuration;

	public static final String ID = AlertType.EXCEPTION;

	protected DateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[CAT异常告警] [项目: ").append(alert.getGroup()).append("]");
		return sb.toString();
	}

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
		return sw.toString();
	}

	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		String domain = alert.getGroup();
		String contactInfo = buildContactInfo(domain);
		Map<Object, Object> map = new HashMap<Object, Object>();

		map.put("domain", domain);
		map.put("content", alert.getContent());
		map.put("date", m_dateFormat.format(alert.getDate()));
		map.put("contactInfo", contactInfo);

		return map;
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
