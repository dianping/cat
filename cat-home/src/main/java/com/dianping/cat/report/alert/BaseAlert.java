package com.dianping.cat.report.alert;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.sender.AlertManager;

public abstract class BaseAlert implements Task, LogEnabled {

	@Inject
	protected AlertInfo m_alertInfo;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected MetricReportGroupService m_service;

	@Inject
	protected AlertManager m_sendManager;

	private static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	protected Logger m_logger;

	protected int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	protected abstract Map<String, ProductLine> getProductlines();

	protected abstract BaseRuleConfigManager getRuleConfigManager();

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

	protected String parseMetricId(String metricKey) {
		try {
			String[] items = metricKey.split(":");

			if (items.length >= 3) {
				return items[2];
			} else {
				Cat.logEvent("ErrorMetricName", metricKey, Event.SUCCESS, null);
				return null;
			}
		} catch (Exception ex) {
			Cat.logError("extract metric name error:" + metricKey, ex);
			return null;
		}
	}

	protected void processProductLine(ProductLine productLine) {
		int minute = calAlreadyMinute();
		String product = productLine.getId();
		AlarmRule alarmRule = getRuleConfigManager().queryConfigs(product);
		int nowMinute = calAlreadyMinute();
		int maxRuleMinute = alarmRule.calMaxRuleMinute();

		if (maxRuleMinute > 0) {
			MetricReportGroup reports = m_service.prepareDatas(product, nowMinute, maxRuleMinute);

			if (reports.isDataReady()) {
				for (Entry<String, MetricItem> metricItem : reports.getMetricItem().entrySet()) {
					try {
						String metricKey = metricItem.getKey();
						String metricName = parseMetricId(metricKey);
						List<Map<MetricType, List<Config>>> detailRules = alarmRule.findDetailRules(metricName);

						for (Map<MetricType, List<Config>> rule : detailRules) {
							for (Entry<MetricType, List<Config>> entry : rule.entrySet()) {
								Pair<Integer, List<Condition>> conditionPair = getRuleConfigManager().convertConditions(
								      entry.getValue());

								if (conditionPair != null) {
									int ruleMinute = conditionPair.getKey();
									MetricType dateType = entry.getKey();
									double[] value = reports.extractData(minute, ruleMinute, metricKey, dateType);

									List<Condition> conditions = conditionPair.getValue();
									List<AlertResultEntity> results = m_dataChecker.checkData(value, conditions);

									if (results.size() > 0) {
										updateAlertStatus(product, metricKey);
										sendAlerts(product, metricName, results);
									}
								}
							}
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			} else {
				Cat.logEvent("AlertDataNotFount", getName(), Event.SUCCESS, null);
			}
		} else {
			Cat.logEvent("NoAlarmRule:" + getName(), product, Event.SUCCESS, null);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			try {
				Transaction t = Cat.newTransaction("Alert" + getName(), TimeHelper.getMinuteStr());
				long current = System.currentTimeMillis();

				try {
					Map<String, ProductLine> productLines = getProductlines();

					for (ProductLine productLine : productLines.values()) {
						try {
							processProductLine(productLine);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
					t.setStatus(Transaction.SUCCESS);
				} catch (Throwable e) {
					t.setStatus(e);
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
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	protected void sendAlerts(String productlineName, String metricName, List<AlertResultEntity> alertResults) {
		for (AlertResultEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(metricName).setType(getName()).setGroup(productlineName);

			m_sendManager.addAlert(entity);
		}
	}

	@Override
	public void shutdown() {
	}

	protected void updateAlertStatus(String productlineName, String metricKey) {
		m_alertInfo.addAlertInfo(productlineName, metricKey, new Date().getTime());
	}
}
