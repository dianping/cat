package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.config.aggregation.AggregationConfigManager;
import com.dianping.cat.configuration.aggreation.model.entity.AggregationRule;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.report.task.alert.AlertConstants;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class AlertExceptionBuilder {

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

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

	public List<AlertException> buildFrontEndAlertExceptions(Item frontEndItem) {
		List<AlertException> alertExceptions = new ArrayList<AlertException>();

		for (Entry<String, Double> entry : frontEndItem.getException().entrySet()) {
			String exception = entry.getKey();
			AggregationRule rule = m_aggregationConfigManager.queryAggration(exception);
			
			if (rule != null) {
				int warn = rule.getWarn();
				double value = entry.getValue().doubleValue();

				if (value >= warn) {
					alertExceptions.add(new AlertException(exception, AlertConstants.WARNING_EXCEPTION, value));
				}
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

			if (!isExcludedException(domain, exceptionName)) {
				double value = entry.getValue().doubleValue();
				Pair<Double, Double> limitPair = queryDomainExceptionLimit(domain, exceptionName);
				double warnLimit = limitPair.getKey();
				double errorLimit = limitPair.getValue();

				totalException += value;

				if (errorLimit > 0 && value >= errorLimit) {
					alertExceptions.add(new AlertException(exceptionName, AlertConstants.ERROR_EXCEPTION, value,
					      needSendSms(domain, exceptionName)));
				} else if (warnLimit > 0 && value >= warnLimit) {
					alertExceptions.add(new AlertException(exceptionName, AlertConstants.WARNING_EXCEPTION, value));
				}
			}
		}

		if (totalErrorLimit > 0 && totalException >= totalErrorLimit) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, AlertConstants.ERROR_EXCEPTION,
			      totalException, needSendSms(domain, ExceptionConfigManager.TOTAL_STRING)));
		} else if (totalWarnLimit > 0 && totalException >= totalWarnLimit) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, AlertConstants.WARNING_EXCEPTION,
			      totalException));
		}

		return alertExceptions;
	}

	private boolean isExcludedException(String domain, String exceptionName) {
		boolean excluded = false;
		ExceptionExclude result = m_exceptionConfigManager.queryDomainExceptionExclude(domain, exceptionName);

		if (result != null) {
			excluded = true;
		}
		return excluded;
	}

	private boolean needSendSms(String domain, String exception) {
		boolean send = false;
		ExceptionLimit limit = m_exceptionConfigManager.queryDomainExceptionLimit(domain, exception);

		if (limit != null) {
			send = limit.getSmsSending();
		}
		return send;
	}

	private Pair<Double, Double> queryDomainExceptionLimit(String domain, String exceptionName) {
		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryDomainExceptionLimit(domain, exceptionName);
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
		ExceptionLimit totalExceptionLimit = m_exceptionConfigManager.queryDomainTotalLimit(domain);
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

		private boolean m_isTriggered;

		public AlertException(String name, String type, double count) {
			m_name = name;
			m_type = type;
			m_count = count;
			m_isTriggered = false;
		}

		public AlertException(String name, String type, double count, boolean isTriggered) {
			m_name = name;
			m_type = type;
			m_count = count;
			m_isTriggered = isTriggered;
		}

		public boolean isTriggered() {
			return m_isTriggered;
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
