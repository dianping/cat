package com.dianping.cat.report.alert.app;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.app.AppAlarmRuleParam;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AppDecorator extends Decorator implements Initializable {

	@Inject
	private AppCommandConfigManager m_appCommandConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public static final String ID = AlertType.App.getName();

	public Configuration m_configuration;

	private AppAlarmDisplay buildAlarmDisplay(AppAlarmRuleParam para) {
		AppAlarmDisplay display = new AppAlarmDisplay();

		display.setCommand(para.getCommand());
		display.setCommandName(para.getCommandName());
		display.setGroupBy(para.getGroupBy());
		display.setMetric(para.getMetric());
		display.setCode(para.getCode() == -1 ? "所有" : String.valueOf(para.getCode()));
		display.setCity(buildFieldValue(para.getCity(), MobileConstants.CITY));
		display.setConnectType((buildFieldValue(para.getConnectType(), MobileConstants.CONNECT_TYPE)));
		display.setNetwork(buildFieldValue(para.getNetwork(), MobileConstants.NETWORK));
		display.setOperator(buildFieldValue(para.getOperator(), MobileConstants.OPERATOR));
		display.setPlatform(buildFieldValue(para.getPlatform(), MobileConstants.PLATFORM));
		display.setVersion(buildFieldValue(para.getVersion(), MobileConstants.VERSION));
		return display;
	}

	private String buildFieldValue(int id, String type) {
		if (id != -1) {
			return m_mobileConfigManager.getConstantItemValue(type, id, "未知 [" + id + "]");
		} else {
			return "所有";
		}
	}

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> dataMap = generateExceptionMap(alert);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("appAlert.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		AppAlarmRuleParam para = (AppAlarmRuleParam) alert.getPara("param");
		AppAlarmDisplay display = buildAlarmDisplay(para);

		map.put("start", alert.getPara("start"));
		map.put("end", alert.getPara("end"));
		map.put("para", display);
		map.put("content", alert.getContent());

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
