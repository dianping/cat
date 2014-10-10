package com.dianping.cat.report.task.alert.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.Monitor;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class WebAlert extends BaseAlert implements Task {

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
			Transaction t = Cat.newTransaction("AlertWeb", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				for (PatternItem item : m_urlPatternConfigManager.queryUrlPatternRules()) {
					try {
						processUrl(item);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				m_currentReports.clear();
				m_lastReports.clear();
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

	private void processUrl(PatternItem item) {
		String url = item.getName();
		String group = item.getGroup();
		List<AlertResultEntity> alertResults = new ArrayList<AlertResultEntity>();
		List<Rule> rules = queryRuelsForUrl(url);
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		for (Rule rule : rules) {
			alertResults = computeAlertForRule(rule, url, minute);

			for (AlertResultEntity alertResult : alertResults) {
				String[] ids = rule.getId().split(":");
				String name = ids[2];
				String type = ids[1];
				Map<String, Object> par = new HashMap<String, Object>();
				par.put("type", queryType(type));
				par.put("name", name);
				AlertEntity entity = new AlertEntity();

				entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
				      .setLevel(alertResult.getAlertLevel());
				entity.setMetric(group).setType(getName()).setGroup(url).setParas(par);
				m_sendManager.addAlert(entity);
			}
		}
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

	protected MetricReport fetchMetricReport(Rule rule, ModelPeriod period) {
		String id = rule.getId();
		String condition = id.split(":")[0];
		MetricReport report = null;

		if (period == ModelPeriod.CURRENT) {
			report = m_currentReports.get(condition);
		} else if (period == ModelPeriod.LAST) {
			report = m_lastReports.get(condition);
		} else {
			throw new RuntimeException("Unknown ModelPeriod: " + period);
		}

		if (report == null) {
			List<String> fields = Splitters.by(";").split(condition);
			String url = fields.get(0);
			String city = fields.get(1);
			String channel = fields.get(2);

			ModelRequest request = new ModelRequest(url, period.getStartTime());
			Map<String, String> pars = new HashMap<String, String>();

			pars.put("metricType", Constants.METRIC_USER_MONITOR);
			pars.put("type", Monitor.TYPE_INFO);
			pars.put("city", city);
			pars.put("channel", channel);
			request.getProperties().putAll(pars);

			report = m_service.invoke(request);

			if (period == ModelPeriod.CURRENT) {
				m_currentReports.put(condition, report);
			} else if (period == ModelPeriod.LAST) {
				m_lastReports.put(condition, report);
			}
		}

		return report;

	}

	private List<Rule> queryRuelsForUrl(String url) {
		List<Rule> rules = new ArrayList<Rule>();

		for (Entry<String, Rule> rule : m_ruleConfigManager.getMonitorRules().getRules().entrySet()) {
			String id = rule.getKey();
			String regexText = id.split(";")[0];

			if (validateRegex(url, regexText)) {
				rules.add(rule.getValue());
			}
		}
		return rules;
	}

	public boolean validateRegex(String regexText, String text) {
		Pattern p = Pattern.compile(regexText);
		Matcher m = p.matcher(text);

		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

	private List<AlertResultEntity> computeAlertForCondition(Map<String, double[]> datas, Condition condition,
	      String type) {
		List<AlertResultEntity> results = new LinkedList<AlertResultEntity>();
		double[] data = datas.get(type);

		if (data != null) {
			results.addAll(m_dataChecker.checkData(data, condition));
		}
		return results;
	}

	private List<AlertResultEntity> computeAlertForRule(Rule rule, String url, int minute) {
		List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();
		Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(rule.getConfigs());
		int maxMinute = resultPair.getKey();
		List<Condition> conditions = resultPair.getValue();
		String type = rule.getId().split(":")[1];

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(rule, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;
				Map<String, double[]> datas = fetchMetricInfoData(start, end, report);

				for (Condition condition : conditions) {
					results.addAll(computeAlertForCondition(datas, condition, type));
				}
			}
		} else if (minute < 0) {
			MetricReport report = fetchMetricReport(rule, ModelPeriod.LAST);

			if (report != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;
				Map<String, double[]> datas = fetchMetricInfoData(start, end, report);

				for (Condition condition : conditions) {
					results.addAll(computeAlertForCondition(datas, condition, type));
				}
			}
		} else {
			MetricReport currentReport = fetchMetricReport(rule, ModelPeriod.CURRENT);
			MetricReport lastReport = fetchMetricReport(rule, ModelPeriod.LAST);

			if (currentReport != null && lastReport != null) {
				int currentStart = 0, currentEnd = minute;
				Map<String, double[]> currentValue = fetchMetricInfoData(currentStart, currentEnd, currentReport);

				int lastStart = 60 + 1 - (maxMinute - minute);
				int lastEnd = 59;
				Map<String, double[]> lastValue = fetchMetricInfoData(lastStart, lastEnd, currentReport);
				Map<String, double[]> datas = new LinkedHashMap<String, double[]>();

				for (Entry<String, double[]> entry : currentValue.entrySet()) {
					String key = entry.getKey();
					double[] current = currentValue.get(key);
					double[] last = lastValue.get(key);

					if (current != null && last != null) {
						datas.put(key, mergerArray(last, current));
					}
				}
				for (Condition condition : conditions) {
					results.addAll(computeAlertForCondition(datas, condition, type));
				}
			}
		}
		return results;
	}

	private Map<String, double[]> fetchMetricInfoData(int start, int end, MetricReport report) {
		Map<String, double[]> datas = new LinkedHashMap<String, double[]>();
		Map<String, double[]> results = new LinkedHashMap<String, double[]>();
		double[] count = new double[60];
		double[] avg = new double[60];
		double[] error = new double[60];
		double[] successPercent = new double[60];

		datas.put("request", count);
		datas.put("delay", avg);
		datas.put("success", successPercent);

		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();
			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				if (key.endsWith(Monitor.HIT)) {
					count[id] = segment.getCount();
				} else if (key.endsWith(Monitor.ERROR)) {
					error[id] = segment.getCount();
				} else if (key.endsWith(Monitor.AVG)) {
					avg[id] = segment.getAvg();
				}
			}
		}

		for (int i = 0; i < 60; i++) {
			double sum = count[i] + error[i];

			if (sum > 0) {
				successPercent[i] = count[i] / sum * 100.0;
			} else {
				successPercent[i] = 100;
			}
		}

		for (Entry<String, double[]> entry : datas.entrySet()) {
			String key = entry.getKey();
			double[] data = entry.getValue();
			int length = end - start + 1;
			double[] result = new double[length];

			System.arraycopy(data, start, result, 0, length);
			results.put(key, result);
		}
		return results;
	}

	@Override
	public String getName() {
		return AlertType.Web.getName();
	}

	@Override
	public void shutdown() {
	}

}
