package com.dianping.cat.report.task.alert.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.app.entity.Command;
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

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

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
					try {
						processRule(entry.getValue());
					} catch (Exception e) {
						Cat.logError(e);
					}
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
		double[] datas = fetchDatas(conditions, type, pair.getKey());

		if (datas != null && datas.length > 0) {
			List<Condition> checkedConditions = pair.getValue();
			List<AlertResultEntity> alertResults = m_dataChecker.checkData(datas, checkedConditions);

			for (AlertResultEntity alertResult : alertResults) {
				Map<String, Object> par = new HashMap<String, Object>();
				par.put("name", array[2]);
				AlertEntity entity = new AlertEntity();

				entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
				      .setLevel(alertResult.getAlertLevel());
				entity.setMetric(queryType(type)).setType(getName()).setGroup(queryCommand(command)).setParas(par);
				m_sendManager.addAlert(entity);
			}
		}
	}

	private QueryEntity buildQueryEntity(String period, String conditions) {
		String split = ";";
		return new QueryEntity(period + split + conditions + split + split);
	}

	private Calendar queryDayPeriod(int day) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	private double[] fetchDatas(String conditions, String type, int minute) {
		long time = (System.currentTimeMillis()) / 1000 / 60;
		int endMinute = (int) (time % (60)) - DATA_AREADY_MINUTE;
		int startMinute = endMinute - minute;
		double[] datas = null;

		if (startMinute < 0 && endMinute < 0) {
			String period = m_sdf.format(queryDayPeriod(-1).getTime());
			QueryEntity queryEntity = buildQueryEntity(period, conditions);
			datas = ArrayUtils.toPrimitive(m_appDataService.queryValue(queryEntity, type), 0);
		} else if (startMinute < 0 && endMinute >= 0) {
			String last = m_sdf.format(queryDayPeriod(-1).getTime());
			String current = m_sdf.format(queryDayPeriod(0).getTime());
			QueryEntity lastQueryEntity = buildQueryEntity(last, conditions);
			QueryEntity currentQueryEntity = buildQueryEntity(current, conditions);
			double[] lastDatas = ArrayUtils.toPrimitive(m_appDataService.queryValue(lastQueryEntity, type), 0);
			double[] currentDatas = ArrayUtils.toPrimitive(m_appDataService.queryValue(currentQueryEntity, type), 0);
			datas = mergerArray(lastDatas, currentDatas);
		} else if (startMinute >= 0) {
			String period = m_sdf.format(queryDayPeriod(0).getTime());
			QueryEntity queryEntity = buildQueryEntity(period, conditions);
			datas = ArrayUtils.toPrimitive(m_appDataService.queryValue(queryEntity, type), 0);
		}
		return datas;
	}

	protected double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private String queryType(String type) {
		String title = "";

		if (AppDataService.SUCCESS.equals(type)) {
			title = "成功率（%/分钟）";
		} else if (AppDataService.REQUEST.equals(type)) {
			title = "请求数（个/分钟）";
		} else if (AppDataService.DELAY.equals(type)) {
			title = "延时平均值（毫秒/分钟）";
		}
		return title;
	}

	private String queryCommand(int command) {
		Map<Integer, Command> commands = m_appConfigManager.getRawCommands();
		Command value = commands.get(command);

		if (value != null) {
			return value.getName();
		} else {
			throw new RuntimeException("Error config in command code: " + command);
		}
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
}
