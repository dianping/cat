package com.dianping.cat.report.task.alert.heartbeat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.service.ProjectService;

public class HeartbeatAlert extends BaseAlert implements Task {

	@Inject
	private ProjectService m_projectService;

	@Inject(type = ModelService.class, value = HeartbeatAnalyzer.ID)
	private ModelService<HeartbeatReport> m_service;

	private static final String[] m_metrics = { "ThreadCount", "DaemonCount", "TotalStartedCount", "CatThreadCount",
	      "PiegonThreadCount", "HttpThreadCount", "NewGcCount", "OldGcCount", "MemoryFree", "HeapUsage",
	      "NoneHeapUsage", "SystemLoadAverage", "CatMessageOverflow", "CatMessageSize" };

	private void buildArray(Map<String, double[]> map, int index, String name, double value) {
		double[] array = map.get(name);
		if (array == null) {
			array = new double[60];
			map.put(name, array);
		}
		array[index] = value;
	}

	private void convertToDeltaArray(Map<String, double[]> map, String name) {
		double[] sources = map.get(name);
		double[] targets = new double[60];

		for (int i = 1; i < 60; i++) {
			if (sources[i - 1] > 0) {
				double delta = sources[i] - sources[i - 1];

				if (delta >= 0) {
					targets[i] = delta;
				}
			}
		}
		map.put(name, targets);
	}

	private double[] extract(double[] lastHourValues, double[] currentHourValues, int maxMinute) {
		int currentLength = currentHourValues.length;
		if (currentLength >= maxMinute) {
			return extract(currentHourValues, maxMinute);
		}

		int lastLength = maxMinute - currentLength;
		double[] result = new double[maxMinute];

		for (int i = 0; i < lastLength; i++) {
			result[i] = lastHourValues[60 - lastLength + i];
		}
		for (int i = lastLength; i < maxMinute; i++) {
			result[i] = currentHourValues[i - lastLength];
		}
		return result;
	}

	private double[] extract(double[] values, int maxMinute) {
		int length = values.length;
		if (length <= maxMinute) {
			return values;
		}

		double[] result = new double[maxMinute];

		for (int i = 0; i < maxMinute; i++) {
			result[i] = values[length - maxMinute + i];
		}
		return result;
	}

	private Map<String, double[]> generateArgumentMap(Machine machine) {
		Map<String, double[]> map = new HashMap<String, double[]>();
		int index = 0;

		for (Period period : machine.getPeriods()) {
			buildArray(map, index, "ThreadCount", period.getThreadCount());
			buildArray(map, index, "DaemonCount", period.getDaemonCount());
			buildArray(map, index, "TotalStartedCount", period.getTotalStartedCount());
			buildArray(map, index, "CatThreadCount", period.getCatThreadCount());
			buildArray(map, index, "PiegonThreadCount", period.getPigeonThreadCount());
			buildArray(map, index, "HttpThreadCount", period.getHttpThreadCount());
			buildArray(map, index, "NewGcCount", period.getNewGcCount());
			buildArray(map, index, "OldGcCount", period.getOldGcCount());
			buildArray(map, index, "MemoryFree", period.getMemoryFree());
			buildArray(map, index, "HeapUsage", period.getHeapUsage());
			buildArray(map, index, "NoneHeapUsage", period.getNoneHeapUsage());
			buildArray(map, index, "SystemLoadAverage", period.getSystemLoadAverage());
			buildArray(map, index, "CatMessageOverflow", period.getCatMessageOverflow());
			buildArray(map, index, "CatMessageSize", period.getCatMessageSize());

			index++;
		}
		convertToDeltaArray(map, "TotalStartedCount");
		convertToDeltaArray(map, "NewGcCount");
		convertToDeltaArray(map, "OldGcCount");
		convertToDeltaArray(map, "CatMessageSize");
		convertToDeltaArray(map, "CatMessageOverflow");
		return map;
	}

	private HeartbeatReport generateReport(String domain, String ip, long date) {
		ModelRequest request = new ModelRequest(domain, date) //
		      .setProperty("ip", ip);

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	public String getName() {
		return AlertType.HeartBeat.getName();
	}

	private void processDomain(String domain) {
		List<Config> configs = m_ruleConfigManager.queryConfigsByGroup(domain);
		int minute = getAlreadyMinute();
		int maxMinute = queryCheckMinuteAndConditions(configs).getKey();

		if (minute >= maxMinute - 1) {
			long currentMill = System.currentTimeMillis();
			long currentHourMill = currentMill - currentMill % TimeHelper.ONE_HOUR;
			HeartbeatReport currentReport = generateReport(domain, null, currentHourMill);
			Set<String> ips = currentReport.getIps();

			for (String ip : ips) {
				Map<String, double[]> arguments = generateArgumentMap(currentReport.getMachines().get(ip));

				for (String metric : m_metrics) {
					try {
						double[] values = extract(arguments.get(metric), maxMinute);

						processMeitrc(domain, ip, metric, values);
					} catch (Exception ex) {
						Cat.logError(ex);
					}
				}
			}
		} else if (minute < 0) {
			long currentMill = System.currentTimeMillis();
			long lastHourMill = currentMill - currentMill % TimeHelper.ONE_HOUR - TimeHelper.ONE_HOUR;
			HeartbeatReport lastReport = generateReport(domain, null, lastHourMill);
			Set<String> ips = lastReport.getIps();

			for (String ip : ips) {
				Map<String, double[]> arguments = generateArgumentMap(lastReport.getMachines().get(ip));

				for (String metric : m_metrics) {
					try {
						double[] values = extract(arguments.get(metric), maxMinute);

						processMeitrc(domain, ip, metric, values);
					} catch (Exception ex) {
						Cat.logError(ex);
					}
				}
			}
		} else {
			long currentMill = System.currentTimeMillis();
			long currentHourMill = currentMill - currentMill % TimeHelper.ONE_HOUR;
			long lastHourMill = currentHourMill - TimeHelper.ONE_HOUR;
			HeartbeatReport currentReport = generateReport(domain, null, currentHourMill);
			HeartbeatReport lastReport = generateReport(domain, null, lastHourMill);
			Set<String> ips = lastReport.getIps();

			for (String ip : ips) {
				Map<String, double[]> lastHourArguments = generateArgumentMap(lastReport.getMachines().get(ip));
				Map<String, double[]> currentHourArguments = generateArgumentMap(currentReport.getMachines().get(ip));

				for (String metric : m_metrics) {
					try {
						double[] values = extract(lastHourArguments.get(metric), currentHourArguments.get(metric), maxMinute);

						processMeitrc(domain, ip, metric, values);
					} catch (Exception ex) {
						Cat.logError(ex);
					}
				}
			}
		}
	}

	private void processMeitrc(String domain, String ip, String metric, double[] values) {
		List<Config> configs = m_ruleConfigManager.queryConfigs(domain, metric);
		Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(configs);
		int maxMinute = resultPair.getKey();
		List<Condition> conditions = resultPair.getValue();
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

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}

		while (active) {
			Transaction t = Cat.newTransaction("AlertHeartbeat", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				for (String domain : m_projectService.findAllDomain()) {
					try {
						processDomain(domain);
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

	@Override
	public void shutdown() {
	}

}
