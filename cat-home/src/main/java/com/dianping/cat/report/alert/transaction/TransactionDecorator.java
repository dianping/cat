package com.dianping.cat.report.alert.transaction;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.Decorator;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TransactionDecorator extends Decorator implements Initializable {

	public static final String ID = AlertType.Transaction.getName();

	protected DateFormat m_linkFormat = new SimpleDateFormat("yyyyMMddHH");

	public Configuration m_configuration;

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> datas = new HashMap<Object, Object>();
		String[] fields = alert.getMetric().split("-");

		datas.put("domain", alert.getGroup());
		datas.put("type", fields[0]);
		datas.put("name", fields[1]);
		datas.put("content", alert.getContent());
		datas.put("date", m_format.format(alert.getDate()));
		datas.put("linkDate", m_linkFormat.format(alert.getDate()));

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("transactionAlert.ftl");
			t.process(datas, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT Transaction告警] [项目: ").append(alert.getGroup()).append("] [监控项: ").append(alert.getMetric())
		      .append("]");
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
