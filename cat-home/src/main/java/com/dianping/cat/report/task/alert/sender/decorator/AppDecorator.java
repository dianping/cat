package com.dianping.cat.report.task.alert.sender.decorator;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AppDecorator extends Decorator implements Initializable {

	public static final String ID = AlertType.App.getName();

	public Configuration m_configuration;

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

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		String type = alert.getMetric();
		System.out.println(alert.getDomain());
		String title = "";

		if (AppDataService.SUCCESS.equals(type)) {
			title = "成功率（%/5分钟）";
		} else if (AppDataService.REQUEST.equals(type)) {
			title = "请求数（个/5分钟）";
		} else if (AppDataService.DELAY.equals(type)) {
			title = "延时平均值（毫秒/5分钟）";
		}

		sb.append("[CAT APP告警] [命令字: " + alert.getGroup() + "] [监控项: ").append(title).append("]");
		return sb.toString();
	}

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

		map.put("content", alert.getContent());
		map.put("date", m_format.format(alert.getDate()));

		return map;
	}

}
