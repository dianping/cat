package com.dianping.cat.report.alert.exception;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.config.web.js.AggregationConfigManager;
import com.dianping.cat.configuration.web.js.entity.AggregationRule;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import com.dianping.cat.report.alert.AlertLevel;

public class AlertExceptionBuilder {

	@Inject
	private ExceptionRuleConfigManager m_exceptionConfigManager;

	@Inject
	private AggregationConfigManager m_aggregationConfigManager;

	public Map<String, List<AlertException>> buildAlertExceptions(List<Item> items) {
		Map<String, List<AlertException>> alertExceptions = new LinkedHashMap<String, List<AlertException>>();

		for (Item item : items) {
			List<AlertException> domainAlertExceptions = buildDomainAlertExceptions(item);

			if (!domainAlertExceptions.isEmpty()) {
				alertExceptions.put(item.getDomain(), domainAlertExceptions);
			}
		}
		return alertExceptions;
	}

	private List<AlertException> buildDomainAlertExceptions(Item item) {
		String domain = item.getDomain();
		List<AlertException> alertExceptions = new ArrayList<AlertException>();
		Pair<Double, Double> totalLimitPair = queryDomainTotalLimit(domain);
		double totalWarnLimit = totalLimitPair.getKey();
		double totalErrorLimit = totalLimitPair.getValue();
		double totalException = 0;

		for (Entry<String, Double> entry : item.getException().entrySet()) {
			String exceptionName = entry.getKey();
			double value = entry.getValue().doubleValue();
			Pair<Double, Double> limitPair = queryDomainExceptionLimit(domain, exceptionName);
			double warnLimit = limitPair.getKey();
			double errorLimit = limitPair.getValue();
			totalException += value;

			if (errorLimit > 0 && value >= errorLimit) {
				alertExceptions.add(new AlertException(exceptionName, AlertLevel.ERROR, value));
			} else if (warnLimit > 0 && value >= warnLimit) {
				alertExceptions.add(new AlertException(exceptionName, AlertLevel.WARNING, value));
			}
		}

		if (totalErrorLimit > 0 && totalException >= totalErrorLimit) {
			alertExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.ERROR,
			      totalException));
		} else if (totalWarnLimit > 0 && totalException >= totalWarnLimit) {
			alertExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.WARNING,
			      totalException));
		}

		return alertExceptions;
	}

	public List<AlertException> buildFrontEndAlertExceptions(Item frontEndItem) {
		List<AlertException> alertExceptions = new ArrayList<AlertException>();

		for (Entry<String, Double> entry : frontEndItem.getException().entrySet()) {
			String exception = entry.getKey();
			AggregationRule rule = m_aggregationConfigManager.queryAggration(exception);

			if (rule != null) {
				int warn = rule.getWarn();
				double value = entry.getValue().doubleValue();

				if (value >= warn) {
					alertExceptions.add(new AlertException(exception, AlertLevel.WARNING, value));
				}
			}
		}
		return alertExceptions;
	}

	private Pair<Double, Double> queryDomainExceptionLimit(String domain, String exceptionName) {
		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryExceptionLimit(domain, exceptionName);
		Pair<Double, Double> limits = new Pair<Double, Double>();
		double warnLimit = -1;
		double errorLimit = -1;

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}
		limits.setKey(warnLimit);
		limits.setValue(errorLimit);

		return limits;
	}

	private Pair<Double, Double> queryDomainTotalLimit(String domain) {
		ExceptionLimit totalExceptionLimit = m_exceptionConfigManager.queryTotalLimitByDomain(domain);
		Pair<Double, Double> limits = new Pair<Double, Double>();
		double totalWarnLimit = -1;
		double totalErrorLimit = -1;

		if (totalExceptionLimit != null) {
			totalWarnLimit = totalExceptionLimit.getWarning();
			totalErrorLimit = totalExceptionLimit.getError();
		}
		limits.setKey(totalWarnLimit);
		limits.setValue(totalErrorLimit);

		return limits;
	}

	public class AlertException {

		private String m_name;

		private String m_type;

		private double m_count;

		public AlertException(String name, String type, double count) {
			m_name = name;
			m_type = type;
			m_count = count;
		}

		public String getName() {
			return m_name;
		}

		public String getType() {
			return m_type;
		}

		@Override
		public String toString() {
			return "[ 异常名称: " + m_name + " 异常数量：" + m_count + " ]";
		}
	}

}
