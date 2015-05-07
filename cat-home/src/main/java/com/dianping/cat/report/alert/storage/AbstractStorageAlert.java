package com.dianping.cat.report.alert.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.AlertResultEntity;
import com.dianping.cat.report.alert.DataChecker;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.sender.AlertManager;
import com.dianping.cat.report.page.storage.StorageConstants;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public abstract class AbstractStorageAlert implements Task, LogEnabled {

	@Inject(type = ModelService.class, value = StorageAnalyzer.ID)
	private ModelService<StorageReport> m_service;

	@Inject
	private StorageMergeHelper m_reportMergeHelper;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected AlertManager m_alertManager;

	@Inject
	protected StorageGroupConfigManager m_storageConfigManager;

	@Inject
	protected StorageAlertInfoBuilder m_alertBuilder;

	protected Logger m_logger;

	private static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	private double[] buildArrayData(int start, int end, ReportFetcherParam param, StorageReport report) {
		String machine = param.getMachine();
		String target = param.getTarget();
		String method = param.getMethod();
		Operation op = report.findOrCreateMachine(machine).findOrCreateDomain(Constants.ALL)
		      .findOrCreateOperation(method);
		Map<Integer, Segment> segments = op.getSegments();
		int length = end - start + 1;
		double[] datas = new double[60];
		double[] result = new double[length];

		if (StorageConstants.COUNT.equalsIgnoreCase(target)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getCount();
			}
		} else if (StorageConstants.LONG.equalsIgnoreCase(target)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getLongCount();
			}
		} else if (StorageConstants.AVG.equals(target)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getAvg();
			}
		} else if (StorageConstants.ERROR.equals(target)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getError();
			}
		} else if (StorageConstants.ERROR_PERCENT.equals(target)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				long count = entry.getValue().getCount();

				if (count > 0) {
					datas[entry.getKey()] = (double) entry.getValue().getError() / count;
				} else {
					datas[entry.getKey()] = 0;
				}
			}
		} else {
			Cat.logError(new RuntimeException("Unrecognized storage databse alert target field: " + target));
		}
		System.arraycopy(datas, start, result, 0, length);

		return result;
	}

	protected int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	private List<AlertResultEntity> computeAlertForRule(int minute, ReportFetcherParam param, List<Config> configs,
	      StorageReport report) {
		List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();
		Pair<Integer, List<Condition>> conditionPair = getRuleConfigManager().convertConditions(configs);

		if (conditionPair != null) {
			int maxMinute = conditionPair.getKey();
			List<Condition> conditions = conditionPair.getValue();

			if (minute >= maxMinute - 1) {
				if (report != null) {
					int start = minute + 1 - maxMinute;
					int end = minute;
					double[] data = buildArrayData(start, end, param, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else if (minute < 0) {
				report = fetchStorageReport(param.getName(), ModelPeriod.LAST);
				if (report != null) {
					int start = 60 + minute + 1 - maxMinute;
					int end = 60 + minute;
					double[] data = buildArrayData(start, end, param, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else {
				StorageReport lastReport = fetchStorageReport(param.getName(), ModelPeriod.LAST);

				if (report != null && lastReport != null) {
					int currentStart = 0, currentEnd = minute;
					double[] currentValue = buildArrayData(currentStart, currentEnd, param, report);
					int lastStart = 60 + 1 - (maxMinute - minute);
					int lastEnd = 59;
					double[] lastValue = buildArrayData(lastStart, lastEnd, param, lastReport);

					double[] data = mergeArray(lastValue, currentValue);
					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			}
		}
		return results;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private StorageReport fetchStorageReport(String name, ModelPeriod period) {
		ModelRequest request = new ModelRequest(name + "-" + getName(), period.getStartTime()) //
		      .setProperty("ip", Constants.ALL).setProperty("requireAll", "true");
		ModelResponse<StorageReport> response = m_service.invoke(request);

		if (response != null) {
			StorageReport report = response.getModel();

			return m_reportMergeHelper.mergeAllDomains(report, Constants.ALL);
		} else {
			return null;
		}
	}

	protected abstract StorageRuleConfigManager getRuleConfigManager();

	protected double[] mergeArray(double[] from, double[] to) {
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

	private void handleAlertInfos(ReportFetcherParam param, int minute, List<AlertResultEntity> alertResults) {
		for (AlertResultEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(param.toString()).setType(getName()).setGroup(param.getName());
			m_alertManager.addAlert(entity);

			m_alertBuilder.processAlertEntity(getName(), minute, entity, param);
		}
	}

	private void processStorage(String id) {
		StorageReport currentReport = fetchStorageReport(id, ModelPeriod.CURRENT);

		if (currentReport != null) {
			for (String ip : currentReport.getIps()) {
				if (m_storageConfigManager.isSQLAlertMachine(id, ip, getName())) {
					processMachine(id, currentReport, ip);
				}
			}
		}
	}

	private void processMachine(String id, StorageReport currentReport, String ip) {
		int minute = calAlreadyMinute();
		boolean alert = true;
		List<Rule> rules = getRuleConfigManager().findRules(id, ip);
		List<Pair<ReportFetcherParam, List<AlertResultEntity>>> alertEntities = new ArrayList<Pair<ReportFetcherParam, List<AlertResultEntity>>>();

		for (Rule rule : rules) {
			ReportFetcherParam param = new ReportFetcherParam(id, ip, rule.getId());

			if (param.getAnd()) {
				if (alert) {
					List<AlertResultEntity> results = computeAlertForRule(minute, param, rule.getConfigs(), currentReport);

					if (results.size() > 0) {
						alertEntities.add(new Pair<ReportFetcherParam, List<AlertResultEntity>>(param, results));
					} else {
						alert = false;
					}
				} else {
					continue;
				}
			} else {
				List<AlertResultEntity> results = computeAlertForRule(minute, param, rule.getConfigs(), currentReport);

				handleAlertInfos(param, minute, results);
			}
		}

		if (alert) {
			for (Pair<ReportFetcherParam, List<AlertResultEntity>> entity : alertEntities) {
				handleAlertInfos(entity.getKey(), minute, entity.getValue());
			}
		}
	}

	private Set<String> queryCurrentStorages() {
		Set<String> ids = new HashSet<String>(m_storageConfigManager.queryStorageGroup(getName()).getStorages().keySet());
		ModelRequest request = new ModelRequest("*-" + getName(), ModelPeriod.CURRENT.getStartTime()) //
		      .setProperty("ip", Constants.ALL);
		ModelResponse<StorageReport> response = m_service.invoke(request);

		if (response != null) {
			StorageReport report = response.getModel();

			if (report != null) {
				ids.addAll(report.getIds());
			}
		}
		return ids;
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("Alert" + getName(), TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Set<String> storages = queryCurrentStorages();

				for (String storage : storages) {
					try {
						processStorage(storage);
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

	public static class ReportFetcherParam {

		private String m_name;

		private String m_machine;

		private String m_method;

		private String m_target;

		private boolean m_and = false;

		public ReportFetcherParam(String name, String machine, String param) {
			List<String> fields = Splitters.by(";").split(param);
			m_name = name;
			m_machine = machine;
			m_method = fields.get(2);
			m_target = fields.get(3);

			if (fields.size() > 4) {
				m_and = Boolean.parseBoolean(fields.get(4));
			}
		}

		public String getMachine() {
			return m_machine;
		}

		public String getMethod() {
			return m_method;
		}

		public String getName() {
			return m_name;
		}

		public String getTarget() {
			return m_target;
		}

		public boolean getAnd() {
			return m_and;
		}

		@Override
		public String toString() {
			return m_machine + ";" + m_method + ";" + m_target;
		}

	}

}
