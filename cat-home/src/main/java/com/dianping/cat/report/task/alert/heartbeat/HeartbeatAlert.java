package com.dianping.cat.report.task.alert.heartbeat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.helper.TimeUtil;
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

	private List<AlertResultEntity> computeArgument(String domain, String ip, String metricText, int minute,
	      double[] datas) {
		String groupText = domain + ":" + ip;
		List<Config> configs = m_ruleConfigManager.queryConfigs(groupText, metricText);
		Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(configs);
		int maxMinute = resultPair.getKey();
		List<Condition> conditions = resultPair.getValue();
		List<AlertResultEntity> alerts = new ArrayList<AlertResultEntity>();

		if (minute >= maxMinute - 1) {
			int start = minute + 1 - maxMinute;
			double[] result = new double[maxMinute];
			System.arraycopy(datas, start, result, 0, maxMinute);

			alerts = m_dataChecker.checkData(result, conditions);
		} else if (minute < 0) {
			long currentMill = new Date().getTime();
			long lastHourMill = currentMill - currentMill % TimeUtil.ONE_HOUR - TimeUtil.ONE_HOUR;
			HeartbeatReport lastHourReport = generateReport(domain, ip, lastHourMill);

			if (lastHourReport != null) {
				Map<String, double[]> lastArguments = generateArgumentMap(lastHourReport.getMachines().get(ip));
				int start = 60 + minute + 1 - maxMinute;
				double[] result = new double[maxMinute];

				System.arraycopy(lastArguments.get(metricText), start, result, 0, maxMinute);
				alerts = m_dataChecker.checkData(result, conditions);
			}
		} else {
			long currentMill = new Date().getTime();
			long lastHourMill = currentMill - currentMill % TimeUtil.ONE_HOUR - TimeUtil.ONE_HOUR;
			HeartbeatReport lastHourReport = generateReport(domain, ip, lastHourMill);
			double[] result = new double[maxMinute];

			if (lastHourReport != null) {
				Map<String, double[]> lastArguments = generateArgumentMap(lastHourReport.getMachines().get(ip));
				int start = 60 + minute + 1 - maxMinute;
				int length = maxMinute - minute - 1;
				System.arraycopy(lastArguments.get(metricText), start, result, 0, length);

				System.arraycopy(datas, 0, result, length + 1, minute + 1);

				alerts = m_dataChecker.checkData(result, conditions);
			}
		}

		return alerts;
	}

	private Map<String, double[]> generateArgumentMap(Machine machine) {
		Map<String, double[]> map = new HashMap<String, double[]>();
		int index = 0;

		for (Period period : machine.getPeriods()) {
			double[] threadCounts = map.get("ThreadCount");
			if (threadCounts == null) {
				threadCounts = new double[60];
				map.put("ThreadCount", threadCounts);
			}
			threadCounts[index] = period.getThreadCount();

			double[] daemonCounts = map.get("DaemonCount");
			if (daemonCounts == null) {
				daemonCounts = new double[60];
				map.put("DaemonCount", daemonCounts);
			}
			daemonCounts[index] = period.getDaemonCount();

			double[] totalStartedCounts = map.get("TotalStartedCount");
			if (totalStartedCounts == null) {
				totalStartedCounts = new double[60];
				map.put("TotalStartedCount", totalStartedCounts);
			}
			totalStartedCounts[index] = period.getTotalStartedCount();

			double[] catThreadCounts = map.get("CatThreadCount");
			if (catThreadCounts == null) {
				catThreadCounts = new double[60];
				map.put("CatThreadCount", catThreadCounts);
			}
			catThreadCounts[index] = period.getCatThreadCount();

			double[] piegonThreadCounts = map.get("PiegonThreadCount");
			if (piegonThreadCounts == null) {
				piegonThreadCounts = new double[60];
				map.put("PiegonThreadCount", piegonThreadCounts);
			}
			piegonThreadCounts[index] = period.getPigeonThreadCount();

			double[] httpThreadCounts = map.get("HttpThreadCount");
			if (httpThreadCounts == null) {
				httpThreadCounts = new double[60];
				map.put("HttpThreadCount", httpThreadCounts);
			}
			httpThreadCounts[index] = period.getHttpThreadCount();

			double[] newGcCounts = map.get("NewGcCount");
			if (newGcCounts == null) {
				newGcCounts = new double[60];
				map.put("NewGcCount", newGcCounts);
			}
			newGcCounts[index] = period.getNewGcCount();

			double[] oldGcCounts = map.get("OldGcCount");
			if (oldGcCounts == null) {
				oldGcCounts = new double[60];
				map.put("OldGcCount", oldGcCounts);
			}
			oldGcCounts[index] = period.getOldGcCount();

			double[] memoryFrees = map.get("MemoryFree");
			if (memoryFrees == null) {
				memoryFrees = new double[60];
				map.put("MemoryFree", memoryFrees);
			}
			memoryFrees[index] = period.getMemoryFree();

			double[] heapUsages = map.get("HeapUsage");
			if (heapUsages == null) {
				heapUsages = new double[60];
				map.put("HeapUsage", heapUsages);
			}
			heapUsages[index] = period.getHeapUsage();

			double[] noneHeapUsages = map.get("NoneHeapUsage");
			if (noneHeapUsages == null) {
				noneHeapUsages = new double[60];
				map.put("NoneHeapUsage", noneHeapUsages);
			}
			noneHeapUsages[index] = period.getNoneHeapUsage();

			double[] systemLoadAverages = map.get("SystemLoadAverage");
			if (systemLoadAverages == null) {
				systemLoadAverages = new double[60];
				map.put("SystemLoadAverage", systemLoadAverages);
			}
			systemLoadAverages[index] = period.getSystemLoadAverage();

			double[] catMessageOverflows = map.get("CatMessageOverflow");
			if (catMessageOverflows == null) {
				catMessageOverflows = new double[60];
				map.put("CatMessageOverflow", catMessageOverflows);
			}
			catMessageOverflows[index] = period.getCatMessageOverflow();

			double[] catMessageSizes = map.get("CatMessageSize");
			if (catMessageSizes == null) {
				catMessageSizes = new double[60];
				map.put("CatMessageSize", catMessageSizes);
			}
			catMessageSizes[index] = period.getCatMessageSize();

			index++;
		}
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

	private void porocessMachine(String domain, Machine machine) {
		String ip = machine.getIp();
		int minute = getAlreadyMinute();
		Map<String, double[]> arguments = generateArgumentMap(machine);

		for (Entry<String, double[]> entry : arguments.entrySet()) {
			String metric = entry.getKey();
			double[] datas = entry.getValue();
			List<AlertResultEntity> alertResults = computeArgument(domain, ip, metric, minute, datas);

			for (AlertResultEntity alertResult : alertResults) {
				AlertEntity entity = new AlertEntity();

				entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
				      .setLevel(alertResult.getAlertLevel());
				entity.setMetric(metric).setType(getName()).setGroup(domain + ":" + ip);

				m_sendManager.addAlert(entity);
			}
		}
	}

	private void processDomain(String domain) {
		long currentMill = new Date().getTime();
		long currentHourMill = currentMill - currentMill % TimeUtil.ONE_HOUR;
		HeartbeatReport report = generateReport(domain, null, currentHourMill);

		for (Machine machine : report.getMachines().values()) {
			try {
				porocessMachine(domain, machine);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
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
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("HeartbeatAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = m_projectService.findAllDomain();

				for (String domain : domains) {
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
