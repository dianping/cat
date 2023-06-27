package com.dianping.cat.report.alert.browser;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.browser.service.AjaxDataQueryEntity;
import com.dianping.cat.report.page.browser.service.AjaxDataService;
import com.dianping.cat.report.page.browser.service.AjaxQueryType;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

@Named
public class AjaxAlert implements Task {

	@Inject
	private AjaxDataService m_webApiService;

	@Inject
	private AlertManager m_sendManager;

	@Inject
	private AjaxRuleConfigManager m_ajaxRuleConfigManager;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	private static final long DURATION = TimeHelper.ONE_MINUTE * 5;

	private static final int DATA_AREADY_MINUTE = 10;

	private Long buildMillsByString(String time) throws Exception {
		String[] times = time.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		long result = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		return result;
	}

	private Pair<Date, Integer> buildTimePair(long time) {
		Pair<Date, Integer> dayAndMinute = new Pair<Date, Integer>();
		Date day = TimeHelper.getCurrentDay(time);
		int minute = (int) ((time - day.getTime()) / TimeHelper.ONE_MINUTE);

		dayAndMinute.setKey(day);
		dayAndMinute.setValue(minute);
		return dayAndMinute;
	}

	private AjaxDataQueryEntity buildAjaxDataQueryEntity(Date date, String conditions, int start, int end) {
		AjaxDataQueryEntity entity = new AjaxDataQueryEntity(date);
		List<String> strs = Splitters.by(";").split(conditions);

		entity.setStartMinuteOrder(start).setEndMinuteOrder(end).setId(strs.get(0)).setCode(strs.get(1))
		      .setCity(strs.get(2)).setOperator(strs.get(3)).setNetwork(strs.get(4));

		return entity;
	}

	private double[] fetchDatas(String conditions, AjaxQueryType type, int minute) {
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime - DATA_AREADY_MINUTE * TimeHelper.ONE_MINUTE;
		long startTime = endTime - minute * TimeHelper.ONE_MINUTE;
		double[] datas = null;

		Pair<Date, Integer> end = buildTimePair(endTime);
		Pair<Date, Integer> start = buildTimePair(startTime);

		if (end.getKey().getTime() == start.getKey().getTime()) {
			AjaxDataQueryEntity queryEntity = buildAjaxDataQueryEntity(end.getKey(), conditions, start.getValue(),
			      end.getValue());
			datas = m_webApiService.queryAlertValue(queryEntity, type);
		} else {
			AjaxDataQueryEntity endQueryEntity = buildAjaxDataQueryEntity(end.getKey(), conditions,
			      AjaxDataQueryEntity.DEFAULT_VALUE, end.getValue());
			AjaxDataQueryEntity startQueryEntity = buildAjaxDataQueryEntity(start.getKey(), conditions, start.getValue(),
			      AjaxDataQueryEntity.DEFAULT_VALUE);
			double[] endDatas = m_webApiService.queryAlertValue(endQueryEntity, type);
			double[] startDatas = m_webApiService.queryAlertValue(startQueryEntity, type);

			datas = mergerArray(endDatas, startDatas);
		}

		return datas;
	}

	@Override
	public String getName() {
		return AlertType.Ajax.getName();
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
		String id = rule.getId();
		int index1 = id.indexOf(":");
		int index2 = id.indexOf(":", index1 + 1);
		String conditions = id.substring(0, index1);
		AjaxQueryType type = AjaxQueryType.findByType(id.substring(index1 + 1, index2));
		String name = id.substring(index2 + 1);
		int api = Integer.valueOf(conditions.split(";")[0]);
		Pair<Integer, List<Condition>> pair = queryCheckMinuteAndConditions(rule.getConfigs());
		double[] datas = fetchDatas(conditions, type, pair.getKey());

		if (datas != null && datas.length > 0) {
			List<Condition> checkedConditions = pair.getValue();
			List<DataCheckEntity> alertResults = m_dataChecker.checkDataForApp(datas, checkedConditions, null);
			String apiName = queryPattern(api);

			for (DataCheckEntity alertResult : alertResults) {
				Map<String, Object> par = new HashMap<String, Object>();
				par.put("name", name);
				AlertEntity entity = new AlertEntity();

				entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
				      .setLevel(alertResult.getAlertLevel());
				entity.setMetric(type.getTitle()).setType(getName()).setGroup(apiName).setParas(par);
				m_sendManager.addAlert(entity);
			}
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

	private String queryPattern(int command) {
		PatternItem item = m_urlPatternConfigManager.queryPatternById(command);

		if (item != null) {
			return item.getName();
		} else {
			throw new RuntimeException("Error config in web api code: " + command);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertAjax", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				MonitorRules monitorRules = m_ajaxRuleConfigManager.getMonitorRules();
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
	public void shutdown() {
	}
}
