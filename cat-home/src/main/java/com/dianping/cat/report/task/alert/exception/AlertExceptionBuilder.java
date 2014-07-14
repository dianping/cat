package com.dianping.cat.report.task.alert.exception;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class AlertExceptionBuilder {

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	public Map<String, List<AlertException>> buildAlertExceptions(List<Item> items) {
		Map<String, List<AlertException>> alertExceptions = new LinkedHashMap<String, List<AlertException>>();

		for (Item item : items) {
			List<AlertException> domainAlertExceptions = buildDomainAlertExceptionList(item);

			if (!domainAlertExceptions.isEmpty()) {
				alertExceptions.put(item.getDomain(), domainAlertExceptions);
			}
		}
		return alertExceptions;
	}

	private List<AlertException> buildDomainAlertExceptionList(Item item) {
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

				if (errorLimit > 0 && value >= errorLimit && needSendSms(domain, exceptionName)) {
					alertExceptions.add(new AlertException(exceptionName, AlertException.ERROR_EXCEPTION, value));
				} else if (warnLimit > 0 && value >= warnLimit) {
					alertExceptions.add(new AlertException(exceptionName, AlertException.WARN_EXCEPTION, value));
				}
			}
		}

		if (totalErrorLimit > 0 && totalException >= totalErrorLimit
		      && needSendSms(domain, ExceptionConfigManager.TOTAL_STRING)) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, AlertException.ERROR_EXCEPTION,
			      totalException));
		} else if (totalWarnLimit > 0 && totalException >= totalWarnLimit) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, AlertException.WARN_EXCEPTION,
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

	public String buildMailContent(String exceptions, String domain) {
		String content = buildContent(exceptions, domain);
		String url = "http://cat.dianpingoa.com/cat/r/p?domain=" + domain;
		String mailContent = content + " <a href='" + url + "'>点击此处查看详情</a>";

		return mailContent;
	}

	public String buildContent(String exceptions, String domain) {
		StringBuilder sb = new StringBuilder();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

		sb.append("[CAT异常告警] [项目: ").append(domain).append("] : ");
		sb.append(exceptions).append("[时间: ").append(time).append("]");

		return sb.toString();
	}

	public List<AlertException> buildErrorException(List<AlertException> exceptions) {
		List<AlertException> errorExceptions = new ArrayList<AlertException>();

		for (AlertException alertException : exceptions) {
			if (AlertException.ERROR_EXCEPTION.equals(alertException.getType())) {
				errorExceptions.add(alertException);
			}
		}
		return errorExceptions;
	}

	public class AlertException {

		private static final String WARN_EXCEPTION = "warn";

		private static final String ERROR_EXCEPTION = "error";

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
			return "{exception_name=" + m_name + ", exception_count=" + m_count + "}";
		}
	}

}
