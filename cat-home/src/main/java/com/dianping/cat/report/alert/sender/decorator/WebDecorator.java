package com.dianping.cat.report.alert.sender.decorator;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class WebDecorator extends Decorator implements Initializable {

	public static final String ID = AlertType.Web.getName();

	public Configuration m_configuration;

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> datas = new HashMap<Object, Object>();

		datas.put("name", alert.getParas().get("name"));
		datas.put("content", alert.getContent());
		datas.put("date", m_format.format(alert.getDate()));

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("webAlert.ftl");
			t.process(datas, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		String type = (String) alert.getMetric();

		sb.append("[CAT Web告警] [URL: ").append(alert.getGroup()).append("] [监控项: ").append(type).append("]");
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
