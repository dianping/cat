package com.dianping.cat.report.task.alert.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertManager;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.site.lookup.util.StringUtils;

public class AppAlert implements Task {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AlertManager m_sendManager;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	private AppConfigManager m_appConfigManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE * 5;

	private static final int DATA_AREADY_MINUTE = 10;

	private static final int ALL_CONDITION = -1;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Map<Integer, String> m_commands = new HashMap<Integer, String>();

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("AppAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				MonitorRules monitorRules = m_appRuleConfigManager.getMonitorRules();
				Map<String, Rule> rules = monitorRules.getRules();

				for (Entry<String, Rule> entry : rules.entrySet()) {
					processRule(entry.getValue());
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public String getName() {
		return AlertType.App.getName();
	}

	@Override
	public void shutdown() {
	}

	private void processRule(Rule rule) {
		String id = rule.getId();
		String[] array = id.split(":");
		String conditions = array[0];
		int command = Integer.valueOf(conditions.split(";")[0]);
		String type = array[1];
		Pair<Integer, List<Condition>> pair = queryCheckMinuteAndConditions(rule.getConfigs());
		QueryEntity queryEntity = buildQueryEntity(pair.getKey(), conditions);
		double[] datas = ArrayUtils.toPrimitive(m_appDataService.queryValue(queryEntity, type), 0);
		List<Condition> checkedConditions = pair.getValue();
		List<AlertResultEntity> alertResults = m_dataChecker.checkData(datas, checkedConditions);

		for (AlertResultEntity alertResult : alertResults) {
			Map<String, Object> par = new HashMap<String, Object>();
			String condition = buildCondition(id);
			par.put("condition", condition);
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(type).setType(getName()).setGroup(queryCommand(command)).setParas(par);
			m_sendManager.addAlert(entity);
		}
	}

	private String queryCommand(int command) {
		String commandStr = m_commands.get(command);

		if (commandStr == null) {
			Map<String, Integer> commandMap = m_appConfigManager.getCommands();

			for (Entry<String, Integer> entry : commandMap.entrySet()) {
				String key = entry.getKey();
				if (entry.getValue() == command) {
					m_commands.put(command, key);
					return key;
				}
			}
		} else {
			return commandStr;
		}
		return "";
	}

	private String queryConfigItemName(int number, String type) {
		if (number != ALL_CONDITION) {
			Map<Integer, Item> itemMap = m_appConfigManager.queryConfigItem(type);
			Item item = itemMap.get(number);

			if (item != null) {
				return item.getName();
			} else {
				return "";
			}
		} else {
			return "All";
		}
	}

	private String queryCode(int command, int code) {
		if (code != ALL_CONDITION) {
			Map<Integer, Code> codeMap = m_appConfigManager.queryCodeByCommand(command);

			if (codeMap != null) {
				Code value = codeMap.get(code);

				if (value != null) {
					return value.getName();
				}
			}
			return "";
		} else {
			return "All";
		}
	}

	private String buildCondition(String id) {
		String[] array = id.split(":");
		List<String> conditions = Splitters.by(";").split(array[0]);
		int command = Integer.valueOf(conditions.get(0));
		int code = Integer.valueOf(conditions.get(1));
		int network = Integer.valueOf(conditions.get(2));
		int version = Integer.valueOf(conditions.get(3));
		int connectType = Integer.valueOf(conditions.get(4));
		int platform = Integer.valueOf(conditions.get(5));
		int city = Integer.valueOf(conditions.get(6));
		int operator = Integer.valueOf(conditions.get(7));

		String commandName = queryCommand(command);
		String codeName = queryCode(command, code);
		String networkName = queryConfigItemName(network, AppConfigManager.NETWORK);
		String versionName = queryConfigItemName(version, AppConfigManager.VERSION);
		String connectTypeName = queryConfigItemName(connectType, AppConfigManager.CONNECT_TYPE);
		String platformName = queryConfigItemName(platform, AppConfigManager.PLATFORM);
		String cityName = queryConfigItemName(city, AppConfigManager.CITY);
		String operatorName = queryConfigItemName(operator, AppConfigManager.OPERATOR);

		List<String> list = Arrays.asList(commandName, codeName, networkName, versionName, connectTypeName, platformName,
		      cityName, operatorName);

		return StringUtils.join(list, ";");
	}

	private Pair<Integer, List<Condition>> queryCheckMinuteAndConditions(List<Config> configs) {
		int maxMinute = 0;
		List<Condition> conditions = new ArrayList<Condition>();
		Iterator<Config> iterator = configs.iterator();

		while (iterator.hasNext()) {
			Config config = iterator.next();

			if (judgeCurrentInConfigRange(config)) {
				List<Condition> tmpConditions = config.getConditions();
				conditions.addAll(tmpConditions);

				for (Condition con : tmpConditions) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}
		return new Pair<Integer, List<Condition>>(maxMinute, conditions);
	}

	private boolean judgeCurrentInConfigRange(Config config) {
		long ruleStartTime;
		long ruleEndTime;
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int nowTime = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		try {
			ruleStartTime = buildMillsByString(config.getStarttime());
			ruleEndTime = buildMillsByString(config.getEndtime());
		} catch (Exception ex) {
			ruleStartTime = 0L;
			ruleEndTime = 86400000L;
		}

		if (nowTime < ruleStartTime || nowTime > ruleEndTime) {
			return false;
		}

		return true;
	}

	private Long buildMillsByString(String time) throws Exception {
		String[] times = time.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		long result = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		return result;
	}

	private QueryEntity buildQueryEntity(int duration, String conditions) {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int endMinute = (int) (current % (60)) - DATA_AREADY_MINUTE;
		int startMinute = endMinute - duration;

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		String period = m_sdf.format(cal.getTime());
		String split = ";";

		return new QueryEntity(period + split + conditions + split + startMinute + split + endMinute);
	}

}
