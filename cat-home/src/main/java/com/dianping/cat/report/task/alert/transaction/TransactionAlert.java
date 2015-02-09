package com.dianping.cat.report.task.alert.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.TransactionMergeHelper;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.config.TransactionRuleConfigManager;

public class TransactionAlert extends BaseAlert {

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	@Inject
	private TransactionMergeHelper m_mergeManager;

	@Inject
	protected TransactionRuleConfigManager m_ruleConfigManager;

	private static String AVG = "avg";

	private static String COUNT = "count";

	private double[] buildArrayData(int start, int end, String type, String name, String monitor,
	      TransactionReport report) {
		TransactionType t = report.findOrCreateMachine(Constants.ALL).findOrCreateType(type);
		TransactionName transactionName = t.findOrCreateName(name);
		Map<Integer, Range> range = transactionName.getRanges();
		int length = end - start + 1;
		double[] datas = new double[60];
		double[] result = new double[length];

		if (AVG.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getAvg();
			}
		} else if (COUNT.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getCount();
			}
		}
		System.arraycopy(datas, start, result, 0, length);

		return result;
	}

	private List<AlertResultEntity> computeAlertForRule(String domain, String type, String name, String monitor,
	      List<Config> configs) {
		List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();
		Pair<Integer, List<Condition>> conditionPair = m_ruleConfigManager.convertConditions(configs);
		int minute = calAlreadyMinute();

		if (conditionPair != null) {
			int maxMinute = conditionPair.getKey();
			List<Condition> conditions = conditionPair.getValue();

			if (StringUtils.isEmpty(name)) {
				name = Constants.ALL;
			}
			if (minute >= maxMinute - 1) {
				TransactionReport report = fetchTransactionReport(domain, type, name, ModelPeriod.CURRENT);

				if (report != null) {
					int start = minute + 1 - maxMinute;
					int end = minute;
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else if (minute < 0) {
				TransactionReport report = fetchTransactionReport(domain, type, name, ModelPeriod.LAST);

				if (report != null) {
					int start = 60 + minute + 1 - (maxMinute);
					int end = 60 + minute;
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else {
				TransactionReport currentReport = fetchTransactionReport(domain, type, name, ModelPeriod.CURRENT);
				TransactionReport lastReport = fetchTransactionReport(domain, type, name, ModelPeriod.LAST);

				if (currentReport != null && lastReport != null) {
					int currentStart = 0, currentEnd = minute;
					double[] currentValue = buildArrayData(currentStart, currentEnd, type, name, monitor, currentReport);
					int lastStart = 60 + 1 - (maxMinute - minute);
					int lastEnd = 59;
					double[] lastValue = buildArrayData(lastStart, lastEnd, type, name, monitor, lastReport);

					double[] data = mergerArray(lastValue, currentValue);
					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			}
		}
		return results;
	}
	
	private TransactionReport fetchTransactionReport(String domain, String type, String name, ModelPeriod period) {
		ModelRequest request = new ModelRequest(domain, period.getStartTime()) //
		      .setProperty("type", type) //
		      .setProperty("name", name)//
		      .setProperty("ip", Constants.ALL);

		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();

		return m_mergeManager.mergerAllName(report, Constants.ALL, name);
	}

	@Override
	public String getName() {
		return AlertType.Transaction.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return null;
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	private void processRule(Rule rule) {
		List<String> fields = Splitters.by(";").split(rule.getId());
		String domain = fields.get(0);
		String type = fields.get(1);
		String name = fields.get(2);
		String monitor = fields.get(3);

		List<AlertResultEntity> alertResults = computeAlertForRule(domain, type, name, monitor, rule.getConfigs());
		for (AlertResultEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(type + "-" + name + "-" + monitor).setType(getName()).setGroup(domain);
			m_sendManager.addAlert(entity);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();
		
		while (active) {
			Transaction t = Cat.newTransaction("AlertTransaction", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				MonitorRules monitorRules = m_ruleConfigManager.getMonitorRules();
				Map<String, Rule> rules = monitorRules.getRules();

				for (Entry<String, Rule> entry : rules.entrySet()) {
					try {
						processRule(entry.getValue());
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

}
