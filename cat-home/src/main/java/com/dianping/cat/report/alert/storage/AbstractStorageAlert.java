package com.dianping.cat.report.alert.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
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
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.storage.StorageMergeHelper;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.StorageRuleConfigManager;

public abstract class AbstractStorageAlert implements Task, LogEnabled {

	@Inject(type = ModelService.class, value = StorageAnalyzer.ID)
	private ModelService<StorageReport> m_service;

	@Inject
	private StorageMergeHelper m_reportMergeHelper;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected AlertManager m_alertManager;

	protected Logger m_logger;

	private static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	private double[] buildArrayData(int start, int end, ReportFetcherParam param, StorageReport report) {
		String machine = param.getMachine();
		String domain = param.getDomain();
		String attribute = param.getAttribute();
		String method = param.getMethod();
		Operation op = report.findOrCreateMachine(machine).findOrCreateDomain(domain).findOrCreateOperation(method);
		Map<Integer, Segment> segments = op.getSegments();
		int length = end - start + 1;
		double[] datas = new double[60];
		double[] result = new double[length];

		if (StorageAttribute.COUNT.equalsIgnoreCase(attribute)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getCount();
			}
		} else if (StorageAttribute.LONG.equalsIgnoreCase(attribute)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getLongCount();
			}
		} else if (StorageAttribute.AVG.equals(attribute)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getAvg();
			}
		} else if (StorageAttribute.ERROR.equals(attribute)) {
			for (Entry<Integer, Segment> entry : segments.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getError();
			}
		} else {
			Cat.logError(new RuntimeException("Unrecognized storage databse alert attribute: " + attribute));
		}
		System.arraycopy(datas, start, result, 0, length);

		return result;
	}

	protected int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	private List<AlertResultEntity> computeAlertForRule(ReportFetcherParam param, List<Config> configs) {
		List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();
		Pair<Integer, List<Condition>> conditionPair = getRuleConfigManager().convertConditions(configs);
		int minute = calAlreadyMinute();

		if (conditionPair != null) {
			int maxMinute = conditionPair.getKey();
			List<Condition> conditions = conditionPair.getValue();

			if (minute >= maxMinute - 1) {
				StorageReport report = fetchStorageReport(param, ModelPeriod.CURRENT);

				if (report != null) {
					int start = minute + 1 - maxMinute;
					int end = minute;
					double[] data = buildArrayData(start, end, param, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else if (minute < 0) {
				StorageReport report = fetchStorageReport(param, ModelPeriod.LAST);

				if (report != null) {
					int start = 60 + minute + 1 - maxMinute;
					int end = 60 + minute;
					double[] data = buildArrayData(start, end, param, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else {
				StorageReport currentReport = fetchStorageReport(param, ModelPeriod.CURRENT);
				StorageReport lastReport = fetchStorageReport(param, ModelPeriod.LAST);

				if (currentReport != null && lastReport != null) {
					int currentStart = 0, currentEnd = minute;
					double[] currentValue = buildArrayData(currentStart, currentEnd, param, currentReport);
					int lastStart = 60 + 1 - (maxMinute - minute);
					int lastEnd = 59;
					double[] lastValue = buildArrayData(lastStart, lastEnd, param, lastReport);

					double[] data = mergerArray(lastValue, currentValue);
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

	private StorageReport fetchStorageReport(ReportFetcherParam param, ModelPeriod period) {
		String name = param.getName();
		String domain = param.getDomain();
		String machine = param.getMachine();
		ModelRequest request = new ModelRequest(name + "-" + getType(), period.getStartTime()) //
		      .setProperty("ip", machine).setProperty("requireAll", "true");
		ModelResponse<StorageReport> response = m_service.invoke(request);

		if (response != null) {
			StorageReport report = response.getModel();

			return m_reportMergeHelper.mergeReport(report, machine, domain);
		} else {
			return null;
		}
	}

	protected abstract StorageRuleConfigManager getRuleConfigManager();

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
		ReportFetcherParam param = new ReportFetcherParam(rule.getId());

		List<AlertResultEntity> alertResults = computeAlertForRule(param, rule.getConfigs());
		for (AlertResultEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(param.toString()).setType(getName()).setGroup(param.getName());
			m_alertManager.addAlert(entity);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("Alert" + getName(), TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Map<String, Rule> monitorRules = getRuleConfigManager().getMonitorRules().getRules();

				for (Entry<String, Rule> entry : monitorRules.entrySet()) {
					try {
						processRule(entry.getValue());
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

			m_alertManager.addAlert(entity);
		}
	}

	@Override
	public void shutdown() {
	}

	protected abstract String getType();

	public static class StorageAttribute {

		public static final String COUNT = "count";

		public static final String LONG = "long";

		public static final String AVG = "avg";

		public static final String ERROR = "error";
	}

	public static class ReportFetcherParam {

		private String m_name;

		private String m_machine;

		private String m_method;

		private String m_attribute;

		private String m_domain;

		public ReportFetcherParam(String param) {
			List<String> fields = Splitters.by(";").split(param);
			m_name = fields.get(0);
			m_machine = fields.get(1);
			m_method = fields.get(2);
			m_attribute = fields.get(3);
			m_domain = fields.get(4);
		}

		public String getName() {
			return m_name;
		}

		public String getMachine() {
			return m_machine;
		}

		public String getMethod() {
			return m_method;
		}

		public String getAttribute() {
			return m_attribute;
		}

		public String getDomain() {
			return m_domain;
		}

		@Override
		public String toString() {
			return m_domain + " " + " " + m_machine + " " + m_method + " " + m_attribute;
		}

	}
}
