package com.dianping.cat.report.alert.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.alert.AlertResultEntity;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.BaseAlert;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;
import com.dianping.cat.report.alert.sender.AlertEntity;

public class WebAlert extends BaseAlert {

	@Inject
	private UrlPatternConfigManager m_urlPatternConfigManager;

	@Inject
	protected WebRuleConfigManager m_ruleConfigManager;

	private List<AlertResultEntity> computeAlertForCondition(Map<String, double[]> datas, List<Condition> conditions,
	      String type) {
		List<AlertResultEntity> results = new LinkedList<AlertResultEntity>();
		double[] data = datas.get(type);

		if (data != null) {
			results.addAll(m_dataChecker.checkData(data, conditions));
		}
		return results;
	}

	private List<AlertResultEntity> computeAlertForRule(String idPrefix, String type, List<Config> configs, String url,
	      int minute) {
		List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();
		Pair<Integer, List<Condition>> conditionPair = m_ruleConfigManager.convertConditions(configs);

		if (conditionPair != null) {
			int maxMinute = conditionPair.getKey();
			List<Condition> conditions = conditionPair.getValue();

			if (minute >= maxMinute - 1) {
				MetricReport report = fetchMetricReport(idPrefix, ModelPeriod.CURRENT);

				if (report != null) {
					int start = minute + 1 - maxMinute;
					int end = minute;
					Map<String, double[]> datas = fetchMetricInfoData(start, end, report);

					results.addAll(computeAlertForCondition(datas, conditions, type));
				}
			} else if (minute < 0) {
				MetricReport report = fetchMetricReport(idPrefix, ModelPeriod.LAST);

				if (report != null) {
					int start = 60 + minute + 1 - (maxMinute);
					int end = 60 + minute;
					Map<String, double[]> datas = fetchMetricInfoData(start, end, report);

					results.addAll(computeAlertForCondition(datas, conditions, type));
				}
			} else {
				MetricReport currentReport = fetchMetricReport(idPrefix, ModelPeriod.CURRENT);
				MetricReport lastReport = fetchMetricReport(idPrefix, ModelPeriod.LAST);

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
					results.addAll(computeAlertForCondition(datas, conditions, type));
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

				if (key.endsWith(Constants.HIT)) {
					count[id] = segment.getCount();
				} else if (key.endsWith(Constants.ERROR)) {
					error[id] = segment.getCount();
				} else if (key.endsWith(Constants.AVG)) {
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

	protected MetricReport fetchMetricReport(String idPrefix, ModelPeriod period) {
		List<String> fields = Splitters.by(";").split(idPrefix);
		String url = fields.get(0);
		String city = fields.get(1);
		String channel = fields.get(2);

		ModelRequest request = new ModelRequest(url, period.getStartTime());
		Map<String, String> pars = new HashMap<String, String>();

		pars.put("metricType", Constants.METRIC_USER_MONITOR);
		pars.put("type", Constants.TYPE_INFO);
		pars.put("city", city);
		pars.put("channel", channel);
		request.getProperties().putAll(pars);

		MetricReport report = m_service.fetchMetricReport(request);

		return report;
	}

	@Override
	public String getName() {
		return AlertType.Web.getName();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	private void processUrl(PatternItem item) {
		String url = item.getName();
		String group = item.getGroup();
		List<AlertResultEntity> alertResults = new ArrayList<AlertResultEntity>();
		List<Rule> rules = queryRuelsForUrl(url);
		int minute = calAlreadyMinute();

		for (Rule rule : rules) {
			String id = rule.getId();
			int index1 = id.indexOf(":");
			int index2 = id.indexOf(":", index1 + 1);
			String idPrefix = id.substring(0, index1);
			String type = id.substring(index1 + 1, index2);
			String name = id.substring(index2 + 1);

			alertResults = computeAlertForRule(idPrefix, type, rule.getConfigs(), url, minute);

			for (AlertResultEntity alertResult : alertResults) {
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

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

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

	public boolean validateRegex(String regexText, String text) {
		Pattern p = Pattern.compile(regexText);
		Matcher m = p.matcher(text);

		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		throw new RuntimeException("Web alert don't support get productline");
	}

}
