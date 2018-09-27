package com.dianping.cat.report.alert.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.app.AppAlarmRuleParam;
import com.dianping.cat.alarm.app.AppAlarmRuleParamBuilder;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.service.AppAlarmRuleInfo;
import com.dianping.cat.alarm.service.AppAlarmRuleService;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppDataField;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.configuration.mobile.entity.ConstantItem;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

@Named
public class AppAlert implements Task {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AlertManager m_sendManager;

	@Inject
	private AppAlarmRuleService m_appAlarmService;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Inject
	private AppAlarmRuleParamBuilder m_alarmRuleParamBuilder;

	private static final long DURATION = TimeHelper.ONE_MINUTE * 5;

	private static final int DATA_AREADY_MINUTE = 10;

	private Map<Integer, List<AppCommandData>> buildDataMap(List<AppCommandData> datas, AppDataField appDataField) {
		Map<Integer, List<AppCommandData>> dataMap = new LinkedHashMap<Integer, List<AppCommandData>>();

		for (AppCommandData data : datas) {
			int value = m_appDataService.queryFieldValue(data, appDataField);
			List<AppCommandData> list = dataMap.get(value);

			if (list == null) {
				list = new LinkedList<AppCommandData>();

				dataMap.put(value, list);
			}
			list.add(data);
		}
		return dataMap;
	}

	private Long buildMillsByString(String time) throws Exception {
		String[] times = time.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		long result = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		return result;
	}

	private List<CommandQueryEntity> buildQueries(long start, long end, AppAlarmRuleParam param) {
		List<CommandQueryEntity> queries = new LinkedList<CommandQueryEntity>();
		long fiveMinutes = TimeHelper.ONE_MINUTE * 5;

		for (long t = end - fiveMinutes; t >= start; t -= fiveMinutes) {
			Date day = TimeHelper.getCurrentDay(t);
			int startMinute = (int) ((t - day.getTime()) / TimeHelper.ONE_MINUTE);
			CommandQueryEntity query = new CommandQueryEntity(day, param, startMinute, startMinute + 4);

			queries.add(query);
		}

		return queries;
	}

	private List<AlarmReusltInfo> buildResultInfos(AppAlarmRuleParam param, Map<Integer, double[]> datas, Date start,
	      Date end) {
		List<AlarmReusltInfo> infos = new LinkedList<AlarmReusltInfo>();

		for (Entry<Integer, double[]> data : datas.entrySet()) {
			try {
				AppAlarmRuleParam p = param.clone();
				int key = data.getKey();

				m_alarmRuleParamBuilder.setField(p, key);
				AlarmReusltInfo info = new AlarmReusltInfo(data.getValue(), p, start, end);

				infos.add(info);
			} catch (Exception e) {
				Cat.logError(e);
			}

		}
		return infos;
	}

	private Pair<Date, Integer> buildTimePair(long time) {
		Pair<Date, Integer> dayAndMinute = new Pair<Date, Integer>();
		Date day = TimeHelper.getCurrentDay(time);
		int minute = (int) ((time - day.getTime()) / TimeHelper.ONE_MINUTE);

		dayAndMinute.setKey(day);
		dayAndMinute.setValue(minute);
		return dayAndMinute;
	}

	private List<AlarmReusltInfo> fetchBatchDatas(AppAlarmRuleParam param, QueryType type, int minute) {
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime - DATA_AREADY_MINUTE * TimeHelper.ONE_MINUTE;
		long startTime = endTime - minute * TimeHelper.ONE_MINUTE;
		List<Map<Integer, Double>> results = new LinkedList<Map<Integer, Double>>();
		List<CommandQueryEntity> queries = buildQueries(startTime, endTime, param);

		for (CommandQueryEntity query : queries) {
			results.add(queryAlertValue(query, type, param.getGroupBy()));
		}

		Map<Integer, double[]> datas = mergeBatchDatas(results);

		return buildResultInfos(param, datas, new Date(startTime), new Date(endTime));
	}

	public AlarmReusltInfo fetchDatas(AppAlarmRuleParam param, QueryType type, int minute) {
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime - DATA_AREADY_MINUTE * TimeHelper.ONE_MINUTE;
		long startTime = endTime - minute * TimeHelper.ONE_MINUTE;

		double[] datas = null;
		Pair<Date, Integer> end = buildTimePair(endTime);
		Pair<Date, Integer> start = buildTimePair(startTime);

		if (end.getKey().getTime() == start.getKey().getTime()) {
			CommandQueryEntity queryEntity = new CommandQueryEntity(end.getKey(), param, start.getValue(),
			      end.getValue() - 1);
			datas = m_appDataService.queryAlertValue(queryEntity, type, minute);
		} else {
			CommandQueryEntity endQueryEntity = new CommandQueryEntity(end.getKey(), param,
			      CommandQueryEntity.DEFAULT_VALUE, end.getValue() - 1);
			CommandQueryEntity startQueryEntity = new CommandQueryEntity(start.getKey(), param, start.getValue(),
			      CommandQueryEntity.DEFAULT_VALUE);
			double[] endDatas = m_appDataService.queryAlertValue(endQueryEntity, type, minute);
			double[] startDatas = m_appDataService.queryAlertValue(startQueryEntity, type, minute);

			datas = mergerArray(endDatas, startDatas);
		}
		return new AlarmReusltInfo(datas, param, new Date(startTime), new Date(endTime));
	}

	@Override
	public String getName() {
		return AlertType.App.getName();
	}

	private void initValue(AppDataField appDataField, Map<Integer, Double> results) {
		ConstantItem constants = m_mobileConfigManager.getConstantItemByCategory(appDataField.getTitle());

		for (Integer items : constants.getItems().keySet()) {
			results.put(items, 0D);
		}
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

	private Map<Integer, double[]> mergeBatchDatas(List<Map<Integer, Double>> results) {
		Map<Integer, double[]> field2Datas = new LinkedHashMap<Integer, double[]>();
		int size = results.size();
		int i = 0;

		for (Map<Integer, Double> result : results) {
			for (Entry<Integer, Double> entry : result.entrySet()) {
				int key = entry.getKey();
				double[] ds = field2Datas.get(key);

				if (ds == null) {
					ds = new double[size];
					field2Datas.put(key, ds);
				}
				ds[i] = entry.getValue();
			}
			i++;
		}
		return field2Datas;
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

	private void processRule(Rule rule) {
		List<AppAlarmRuleParam> params = m_alarmRuleParamBuilder.build(rule);

		for (AppAlarmRuleParam param : params) {

			QueryType queryType = QueryType.findByName(param.getMetric());
			int command = param.getCommand();

			Pair<Integer, List<Condition>> pair = queryCheckMinuteAndConditions(rule.getConfigs());
			List<AlarmReusltInfo> infos = new LinkedList<AppAlert.AlarmReusltInfo>();

			if (param.isEachAlarm()) {
				infos.addAll(fetchBatchDatas(param, queryType, pair.getKey()));
			} else {
				infos.add(fetchDatas(param, queryType, pair.getKey()));
			}
			sendAlarm(queryType, command, pair.getValue(), infos);
		}
	}

	private Map<Integer, Double> queryAlertValue(CommandQueryEntity query, QueryType type, AppDataField appDataField) {
		List<AppCommandData> datas = queryByAlarm(query, type, appDataField);
		Map<Integer, Double> results = new LinkedHashMap<Integer, Double>();

		switch (type) {
		case NETWORK_SUCCESS:
		case BUSINESS_SUCCESS:
			Map<Integer, List<AppCommandData>> dataMap = buildDataMap(datas, appDataField);

			for (Entry<Integer, List<AppCommandData>> entry : dataMap.entrySet()) {
				double value = m_appDataService.computeSuccessRatio(query.getId(), entry.getValue(), type);

				results.put(entry.getKey(), value);
			}
			break;
		case REQUEST:
			initValue(appDataField, results);

			for (AppCommandData data : datas) {
				int filed = m_appDataService.queryFieldValue(data, appDataField);

				results.put(filed, (double) data.getAccessNumberSum());
			}
			break;
		case DELAY:
			for (AppCommandData data : datas) {
				long accessSumNum = data.getAccessNumberSum();
				int field = m_appDataService.queryFieldValue(data, appDataField);
				double value = 0;

				if (accessSumNum > 0) {
					value = data.getResponseSumTimeSum() / accessSumNum;
				}

				results.put(field, value);
			}
			break;
		case REQUEST_PACKAGE:
		case RESPONSE_PACKAGE:
			throw new RuntimeException("unexpected query type, type:" + type);
		}
		return results;
	}

	public List<AppCommandData> queryByAlarm(CommandQueryEntity entity, QueryType type, AppDataField groupByField) {
		List<AppCommandData> datas = new ArrayList<AppCommandData>();

		try {
			switch (type) {
			case NETWORK_SUCCESS:
			case BUSINESS_SUCCESS:
				datas = m_appDataService.queryByFieldCode(entity, groupByField);
				break;
			case REQUEST:
			case DELAY:
				datas = m_appDataService.queryByField(entity, groupByField);
				break;
			case REQUEST_PACKAGE:
			case RESPONSE_PACKAGE:
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
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

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertApp", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Map<String, List<AppAlarmRuleInfo>> alarmRules = m_appAlarmService.queryAllRules();

				for (List<AppAlarmRuleInfo> rules : alarmRules.values()) {
					for (AppAlarmRuleInfo rule : rules) {
						try {
							processRule(rule.getRule());
						} catch (Exception e) {
							Cat.logError(e);
						}
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

	private void sendAlarm(QueryType queryType, int command, List<Condition> checkedConditions,
	      List<AlarmReusltInfo> results) {
		for (AlarmReusltInfo result : results) {
			try {
				double[] datas = result.getDatas();

				if (datas != null && datas.length > 0) {
					List<DataCheckEntity> alertResults = m_dataChecker.checkDataForApp(datas, checkedConditions);
					AppAlarmRuleParam param = result.getParam();
					String commandName = param.getCommandName();

					for (DataCheckEntity alertResult : alertResults) {
						try {
							Map<String, Object> par = new HashMap<String, Object>();

							par.put("param", param);
							par.put("start", result.getStart());
							par.put("end", result.getEnd());
							AlertEntity entity = new AlertEntity();

							entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
							      .setLevel(alertResult.getAlertLevel());
							entity.setMetric(queryType.getTitle()).setType(getName()).setGroup(commandName).setParas(par);
							m_sendManager.addAlert(entity);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void shutdown() {
	}

	public static class AlarmReusltInfo {

		private double[] m_datas;

		private AppAlarmRuleParam m_param;

		private Date m_start;

		private Date m_end;

		public AlarmReusltInfo(double[] datas, AppAlarmRuleParam param, Date start, Date end) {
			m_datas = datas;
			m_param = param;
			m_start = start;
			m_end = end;
		}

		public double[] getDatas() {
			return m_datas;
		}

		public Date getEnd() {
			return m_end;
		}

		public AppAlarmRuleParam getParam() {
			return m_param;
		}

		public Date getStart() {
			return m_start;
		}

	}
}
