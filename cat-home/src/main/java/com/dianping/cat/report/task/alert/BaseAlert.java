package com.dianping.cat.report.task.alert;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.productline.ProductLineConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.MetricReportGroup.State;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertManager;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.BaseRuleConfigManager;

public abstract class BaseAlert implements Task, LogEnabled {

	@Inject
	protected AlertInfo m_alertInfo;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected RemoteMetricReportService m_service;

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

	protected MetricReport fetchMetricReport(String product, ModelPeriod period) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");
		MetricReport report = m_service.invoke(request);

		return report;
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
			return metricKey.split(":")[2];
		} catch (Exception ex) {
			Cat.logError("extract metric name error:" + metricKey, ex);
			return null;
		}
	}

	protected MetricReportGroup prepareDatas(String product, int duration) {
		int minute = calAlreadyMinute();
		MetricReport currentReport = null;
		MetricReport lastReport = null;
		boolean dataReady = false;
		State type = null;

		if (minute >= duration - 1) {
			type = State.CURRENT;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (currentReport != null) {
				dataReady = true;
			}
		} else if (minute < 0) {
			type = State.LAST;
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				dataReady = true;
			}
		} else {
			type = State.CURRENT_LAST;
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null && currentReport != null) {
				dataReady = true;
			}
		}
		MetricReportGroup reports = new MetricReportGroup();

		reports.setType(type).setLast(lastReport).setCurrent(currentReport).setDataReady(dataReady);
		return reports;
	}

	protected void processProductLine(ProductLine productLine) {
		int minute = calAlreadyMinute();
		String product = productLine.getId();
		AlarmRule alarmRule = getRuleConfigManager().queryConfigs(product);
		int maxMinute = alarmRule.calMaxMinute();
		MetricReportGroup reports = prepareDatas(product, maxMinute);

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
	}

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
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
			} catch (Exception e) {
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
