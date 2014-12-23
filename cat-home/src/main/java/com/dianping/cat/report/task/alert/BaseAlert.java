package com.dianping.cat.report.task.alert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
import com.dianping.cat.message.Transaction;
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

	@Inject
	protected DataExtractor m_dataExtractor;

	protected static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	protected Logger m_logger;

	private int calMaxMinute(Map<String, Map<MetricType, List<Config>>> configs) {
		int maxMinute = 0;

		for (Map<MetricType, List<Config>> subMap : configs.values()) {
			for (List<Config> tmpConfigs : subMap.values()) {
				for (Config config : tmpConfigs) {
					for (Condition condition : config.getConditions()) {
						int tmpMinute = condition.getMinute();

						if (tmpMinute > maxMinute) {
							maxMinute = tmpMinute;
						}
					}
				}
			}
		}
		return maxMinute;
	}

	private boolean compareTime(String start, String end) {
		String[] startTime = start.split(":");
		int hourStart = Integer.parseInt(startTime[0]);
		int minuteStart = Integer.parseInt(startTime[1]);
		int startMinute = hourStart * 60 + minuteStart;

		String[] endTime = end.split(":");
		int hourEnd = Integer.parseInt(endTime[0]);
		int minuteEnd = Integer.parseInt(endTime[1]);
		int endMinute = hourEnd * 60 + minuteEnd;

		Calendar cal = Calendar.getInstance();
		int current = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

		return current >= startMinute && current <= endMinute;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	protected String extractDomain(String metricKey) {
		try {
			return metricKey.split(":")[0];
		} catch (Exception ex) {
			Cat.logError("extract domain error:" + metricKey, ex);
			return null;
		}
	}

	protected String extractMetricName(String metricKey) {
		try {
			return metricKey.split(":")[2];
		} catch (Exception ex) {
			Cat.logError("extract metric name error:" + metricKey, ex);
			return null;
		}
	}

	protected MetricReport fetchMetricReport(String product, ModelPeriod period) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");
		MetricReport report = m_service.invoke(request);

		return report;
	}

	protected int getAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	protected abstract Map<String, ProductLine> getProductlines();

	protected abstract BaseRuleConfigManager getRuleConfigManager();

	private boolean judgeCurrentInConfigRange(Config config) {
		try {
			if (compareTime(config.getStarttime(), config.getEndtime())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			Cat.logError("throw exception when judge time: " + config.toString(), ex);
			return false;
		}
	}

	private void processMetricItem(int minute, String product, String metricKey,
	      Map<String, Map<MetricType, List<Config>>> configs, MetricReport lastReport, MetricReport currentReport) {
		String metricName = extractMetricName(metricKey);

		for (Entry<String, Map<MetricType, List<Config>>> entry : configs.entrySet()) {
			String metricPattern = entry.getKey();

			if (getRuleConfigManager().validateRegex(metricPattern, metricName) > 0) {
				for (Entry<MetricType, List<Config>> configsByType : entry.getValue().entrySet()) {
					MetricType currentType = configsByType.getKey();
					Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(configsByType.getValue());
					int maxMinute = resultPair.getKey();
					Pair<double[], double[]> datas = m_dataExtractor.extractData(minute, maxMinute, lastReport,
					      currentReport, metricKey, currentType);

					double[] baseline = datas.getKey();
					double[] value = datas.getValue();
					List<Condition> conditions = resultPair.getValue();
					List<AlertResultEntity> results = m_dataChecker.checkData(value, baseline, conditions);

					sendAlerts(product, metricKey, metricName, results);
				}
			}
		}
	}

	protected void processProductLine(ProductLine productLine) {
		int minute = getAlreadyMinute();
		String product = productLine.getId();
		Map<String, Map<MetricType, List<Config>>> configs = getRuleConfigManager().queryConfigs(product);
		int maxMinute = calMaxMinute(configs);
		MetricReport currentReport = null;
		MetricReport lastReport = null;
		boolean isDataReady = false;

		if (minute >= maxMinute - 1) {
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (currentReport != null) {
				isDataReady = true;
			}
		} else if (minute < 0) {
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				isDataReady = true;
			}
		} else {
			currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null && currentReport != null) {
				isDataReady = true;
			}
		}

		if (isDataReady) {
			MetricReport report = currentReport == null ? lastReport : currentReport;

			for (Entry<String, MetricItem> entry : report.getMetricItems().entrySet()) {
				try {
					processMetricItem(minute, product, entry.getKey(), configs, lastReport, currentReport);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	protected Pair<Integer, List<Condition>> queryCheckMinuteAndConditions(List<Config> configs) {
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
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
			Transaction t = Cat.newTransaction("alert-" + getName(), TimeHelper.getMinuteStr());
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

	protected void sendAlerts(String productlineName, String metricKey, String metricName,
	      List<AlertResultEntity> alertResults) {
		for (AlertResultEntity alertResult : alertResults) {
			m_alertInfo.addAlertInfo(productlineName, metricKey, new Date().getTime());

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
}
