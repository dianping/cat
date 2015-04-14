package com.dianping.cat.report.alert.sender.decorator;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FrontEndExceptionDecorator extends ProjectDecorator implements Initializable {

	public static final String ID = AlertType.FrontEndException.getName();

	public Configuration m_configuration;

	protected DateFormat m_linkFormat = new SimpleDateFormat("yyyyMMddHH");

	@Override
	public String buildContactInfo(String domainName) {
		return "";
	}

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> dataMap = generateExceptionMap(alert);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("exceptionAlert.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		String domain = Constants.FRONT_END;
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
		sb.append("[CAT 前端异常告警] [页面: ").append(alert.getGroup()).append("]");
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
