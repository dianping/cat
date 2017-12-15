package com.dianping.cat.report.alert.sender.decorator;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AppDecorator extends Decorator implements Initializable {

	public static final String ID = AlertType.App.getName();

	public Configuration m_configuration;

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> dataMap = generateExceptionMap(alert);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("appAlert.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("name", alert.getParas().get("name"));
		map.put("content", alert.getContent());
		map.put("date", m_format.format(alert.getDate()));

		return map;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		String type = alert.getMetric();

		sb.append("[CAT APP告警] [命令字: ").append(alert.getGroup()).append("] [监控项: ").append(type).append("]");
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
