package com.dianping.cat.report.alert.heartbeat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.report.alert.AlertResultEntity;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.BaseAlert;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;
import com.dianping.cat.report.alert.sender.AlertEntity;

public class HeartbeatAlert extends BaseAlert {

	@Inject(type = ModelService.class, value = HeartbeatAnalyzer.ID)
	private ModelService<HeartbeatReport> m_heartbeatService;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	@Inject
	private HeartbeatDisplayPolicyManager m_displayManager;

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	protected HeartbeatRuleConfigManager m_ruleConfigManager;

	private Map<String, double[]> buildArrayForExtensions(List<Period> periods) {
		Map<String, double[]> map = new LinkedHashMap<String, double[]>();

		for (Period period : periods) {
			List<Pair<String, String>> metrics = extractExtentionMetrics(period);
			int index = period.getMinute();

			for (Pair<String, String> metric : metrics) {
				String key = metric.getKey() + ":" + metric.getValue();
				double[] array = map.get(key);

				if (array == null) {
					array = new double[60];
					map.put(key, array);
				}
				try {
					String groupName = metric.getKey();
					String metricName = metric.getValue();
					int unit = m_displayManager.queryUnit(groupName, metricName);
					Detail detail = period.findOrCreateExtension(groupName).findOrCreateDetail(metricName);

					array[index] = detail.getValue() / unit;
				} catch (Exception e) {
					array[index] = 0;
				}
			}
		}
		return map;
	}

	private int calMaxMinute(Map<String, List<Config>> configs) {
		int maxMinute = 0;

		for (List<Config> tmpConfigs : configs.values()) {
			for (Config config : tmpConfigs) {
				for (Condition condition : config.getConditions()) {
					int tmpMinute = condition.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}
		return maxMinute;
	}

	private double[] extract(double[] lastHourValues, double[] currentHourValues, int maxMinute, int alreadyMinute) {
		if (lastHourValues != null && currentHourValues != null) {
			int lastLength = maxMinute - alreadyMinute - 1;
			double[] result = new double[maxMinute];

			for (int i = 0; i < lastLength; i++) {
				result[i] = lastHourValues[60 - lastLength + i];
			}
			for (int i = lastLength; i < maxMinute; i++) {
				result[i] = currentHourValues[i - lastLength];
			}
			return result;
		} else {
			return null;
		}
	}

	private double[] extract(double[] values, int maxMinute, int alreadyMinute) {
		if (values != null) {
			double[] result = new double[maxMinute];

			for (int i = 0; i < maxMinute; i++) {
				result[i] = values[alreadyMinute + 1 - maxMinute + i];
			}
			return result;
		} else {
			return null;
		}
	}

	private List<Pair<String, String>> extractExtentionMetrics(Period period) {
		List<Pair<String, String>> metrics = new ArrayList<Pair<String, String>>();

		for (Extension extension : period.getExtensions().values()) {
			Map<String, Detail> details = extension.getDetails();

			for (Entry<String, Detail> detail : details.entrySet()) {
				metrics.add(new Pair<String, String>(extension.getId(), detail.getKey()));
			}
		}
		return metrics;
	}

	private Map<String, double[]> buildBaseValue(Machine machine) {
		Map<String, double[]> map = buildArrayForExtensions(machine.getPeriods());

		for (String id : map.keySet()) {
			String[] str = id.split(":");

			if (m_displayManager.isDelta(str[0], str[1])) {
				double[] sources = map.get(id);
				double[] targets = new double[60];

				for (int i = 1; i < 60; i++) {
					if (sources[i - 1] > 0) {
						double delta = sources[i] - sources[i - 1];

						if (delta >= 0) {
							targets[i] = delta;
						}
					}
				}
				map.put(id, targets);
			}
		}

		return map;
	}

	private HeartbeatReport generateCurrentReport(String domain) {
		long currentMill = System.currentTimeMillis();
		long currentHourMill = currentMill - currentMill % TimeHelper.ONE_HOUR;

		return generateReport(domain, currentHourMill);
	}

	private HeartbeatReport generateLastReport(String domain) {
		long currentMill = System.currentTimeMillis();
		long lastHourMill = currentMill - currentMill % TimeHelper.ONE_HOUR - TimeHelper.ONE_HOUR;

		return generateReport(domain, lastHourMill);
	}

	private HeartbeatReport generateReport(String domain, long date) {
		ModelRequest request = new ModelRequest(domain, date)//
		      .setProperty("ip", Constants.ALL).setProperty("requireAll", "true");

		if (m_heartbeatService.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_heartbeatService.invoke(request);

			if (response != null) {
				return response.getModel();
			} else {
				return null;
			}
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	public String getName() {
		return AlertType.HeartBeat.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		throw new RuntimeException("get productline is not support by heartbeat alert");
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	private void processDomain(String domain) {
		int minute = calAlreadyMinute();
		Map<String, List<Config>> configsMap = m_ruleConfigManager.queryConfigsByDomain(domain);
		int domainMaxMinute = calMaxMinute(configsMap);
		HeartbeatReport currentReport = null;
		HeartbeatReport lastReport = null;
		boolean isDataReady = false;

		if (minute >= domainMaxMinute - 1) {
			currentReport = generateCurrentReport(domain);

			if (currentReport != null) {
				isDataReady = true;
			}
		} else if (minute < 0) {
			lastReport = generateLastReport(domain);

			if (lastReport != null) {
				isDataReady = true;
			}
		} else {
			currentReport = generateCurrentReport(domain);
			lastReport = generateLastReport(domain);

			if (lastReport != null && currentReport != null) {
				isDataReady = true;
			}
		}

		if (isDataReady) {
			for (Entry<String, List<Config>> entry : configsMap.entrySet()) {
				String metric = entry.getKey();
				List<Config> configs = entry.getValue();
				Pair<Integer, List<Condition>> conditionPair = m_ruleConfigManager.convertConditions(configs);

				if (conditionPair != null) {
					int maxMinute = conditionPair.getKey();
					List<Condition> conditions = conditionPair.getValue();

					if (minute >= maxMinute - 1) {
						for (Machine machine : currentReport.getMachines().values()) {
							String ip = machine.getIp();
							double[] arguments = buildBaseValue(machine).get(metric);

							if (arguments != null) {
								double[] values = extract(arguments, maxMinute, minute);

								processMeitrc(domain, ip, metric, conditions, maxMinute, values);
							}
						}
					} else if (minute < 0) {
						for (Machine machine : lastReport.getMachines().values()) {
							String ip = machine.getIp();
							double[] arguments = buildBaseValue(machine).get(metric);

							if (arguments != null) {
								double[] values = extract(arguments, maxMinute, 59);

								processMeitrc(domain, ip, metric, conditions, maxMinute, values);
							}
						}
					} else {
						for (Machine lastMachine : lastReport.getMachines().values()) {
							String ip = lastMachine.getIp();
							Machine currentMachine = currentReport.getMachines().get(ip);

							if (currentMachine != null) {
								Map<String, double[]> lastHourArguments = buildBaseValue(lastMachine);
								Map<String, double[]> currentHourArguments = buildBaseValue(currentMachine);

								if (lastHourArguments != null && currentHourArguments != null) {
									double[] values = extract(lastHourArguments.get(metric), currentHourArguments.get(metric),
									      maxMinute, minute);

									processMeitrc(domain, ip, metric, conditions, maxMinute, values);
								}
							}
						}
					}
				}
			}
		}
	}

	private void processMeitrc(String domain, String ip, String metric, List<Condition> conditions, int maxMinute,
	      double[] values) {
		try {
			if (values != null) {
				double[] baseline = new double[maxMinute];
				List<AlertResultEntity> alerts = m_dataChecker.checkData(values, baseline, conditions);

				for (AlertResultEntity alertResult : alerts) {
					AlertEntity entity = new AlertEntity();

					entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
					      .setLevel(alertResult.getAlertLevel());
					entity.setMetric(metric).setType(getName()).setGroup(domain);
					entity.getParas().put("ip", ip);
					m_sendManager.addAlert(entity);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private Set<String> queryDomains() {
		Set<String> domains = new HashSet<String>();
		ModelRequest request = new ModelRequest("cat", System.currentTimeMillis());

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			domains.addAll(response.getModel().getDomainNames());
		}

		return domains;
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertHeartbeat", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = queryDomains();

				for (String domain : domains) {
					if (m_configManager.validateDomain(domain) && StringUtils.isNotEmpty(domain)) {
						try {
							processDomain(domain);
						} catch (Exception e) {
							Cat.logError(e);
						}
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

}
