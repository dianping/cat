package com.dianping.cat.alarm.app;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.app.AppDataField;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Named
public class AppAlarmRuleParamBuilder {

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public static final String COMMAND = "command";

	public static final String COMMAND_NAME = "commandName";

	public static final String CODE = "code";

	public static final String NETWORK = MobileConstants.NETWORK;

	public static final String VERSION = MobileConstants.VERSION;

	public static final String CONNECT_TYPE = MobileConstants.CONNECT_TYPE;

	public static final String PLATFORM = MobileConstants.PLATFORM;

	public static final String CITY = MobileConstants.CITY;

	public static final String OPERATOR = MobileConstants.OPERATOR;

	public static final String METRIC = "metric";

	public List<AppAlarmRuleParam> build(Rule rule) {
		List<AppAlarmRuleParam> results = new ArrayList<AppAlarmRuleParam>();
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		List<String> starKeys = new ArrayList<String>();

		for (Entry<String, String> entry : rule.getDynamicAttributes().entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if ("*".equals(value)) {
				value = "-1";
				starKeys.add(key);
			}
			attributes.put(key, value);
		}

		if (starKeys.size() > 0) {
			for (String key : starKeys) {
				try {
					AppAlarmRuleParam param = buildParam(attributes);
					AppDataField dataField = AppDataField.getByTitle(key);

					param.setGroupBy(dataField);
					results.add(param);
				} catch (Exception e) {
					Cat.logError(rule.toString(), e);
				}
			}
		} else {
			results.add(buildParam(attributes));
		}
		return results;
	}

	private AppAlarmRuleParam buildParam(Map<String, String> attrs) throws NumberFormatException {
		int command = Integer.parseInt(attrs.get(COMMAND));
		String commandName = attrs.get(COMMAND_NAME);
		int code = Integer.parseInt(attrs.get(CODE));
		int network = Integer.parseInt(attrs.get(NETWORK));
		int version = Integer.parseInt(attrs.get(VERSION));
		int connectType = Integer.parseInt(attrs.get(CONNECT_TYPE));
		int platform = Integer.parseInt(attrs.get(PLATFORM));
		int city = Integer.parseInt(attrs.get(CITY));
		int operator = Integer.parseInt(attrs.get(OPERATOR));
		String metric = attrs.get(METRIC);

		AppAlarmRuleParam param = new AppAlarmRuleParam();

		param.setCommand(command);
		param.setCommandName(commandName);
		param.setCode(code);
		param.setNetwork(network);
		param.setVersion(version);
		param.setConnectType(connectType);
		param.setPlatform(platform);
		param.setCity(city);
		param.setOperator(operator);
		param.setMetric(metric);
		return param;
	}

	public void setField(AppAlarmRuleParam param, int value) {
		switch (param.getGroupBy()) {
		case OPERATOR:
			param.setOperator(value);
			break;
		case APP_VERSION:
			param.setVersion(value);
			break;
		case CITY:
			param.setCity(value);
			break;
		case CONNECT_TYPE:
			param.setConnectType(value);
			break;
		case NETWORK:
			param.setNetwork(value);
			break;
		case PLATFORM:
			param.setPlatform(value);
			break;
		case CODE:
		case SOURCE:
			break;
		}
	}

	public void setMobileConfigManager(MobileConfigManager manager) {
		m_mobileConfigManager = manager;
	}
}
