package com.dianping.cat.report.task.alert.exception;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.top.TopMetric;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public class ExceptionAlert implements Task, LogEnabled {

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private ExceptionAlertConfig m_alertConfig;

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int ALERT_PERIOD = 1;

	private static final int WARN_FLAG = 1;

	private static final int ERROR_FLAG = 2;

	private Logger m_logger;

	private TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, m_exceptionConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeUtil.ONE_MINUTE));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

	private boolean isExcludedException(String domain, String exceptionName) {
		boolean excluded = false;
		ExceptionExclude result = m_exceptionConfigManager.queryDomainExceptionExclude(domain, exceptionName);

		if (result != null) {
			excluded = true;
		}
		return excluded;
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

				if (errorLimit > 0 && value > errorLimit && sendSms(domain, exceptionName)) {
					alertExceptions.add(new AlertException(exceptionName, ERROR_FLAG, value));
				} else if (warnLimit > 0 && value > warnLimit) {
					alertExceptions.add(new AlertException(exceptionName, WARN_FLAG, value));
				}
			}
		}

		if (totalErrorLimit > 0 && totalException > totalErrorLimit
		      && sendSms(domain, ExceptionConfigManager.TOTAL_STRING)) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, ERROR_FLAG, totalException));
		} else if (totalWarnLimit > 0 && totalException > totalWarnLimit) {
			alertExceptions.add(new AlertException(ExceptionConfigManager.TOTAL_STRING, WARN_FLAG, totalException));
		}

		return alertExceptions;
	}

	private boolean sendSms(String domain, String exception) {
		boolean send = false;
		ExceptionLimit limit = m_exceptionConfigManager.queryDomainExceptionLimit(domain, exception);

		if (limit != null) {
			send = limit.getSmsSending();
		}
		return send;
	}

	private Map<String, List<AlertException>> buildAlertExceptions(List<Item> items) {
		Map<String, List<AlertException>> alertExceptions = new LinkedHashMap<String, List<AlertException>>();

		// different domain -> [excepitons:numbers]
		for (Item item : items) {
			List<AlertException> domainAlertExceptions = buildDomainAlertExceptionList(item);

			if (!domainAlertExceptions.isEmpty()) {
				alertExceptions.put(item.getDomain(), domainAlertExceptions);
			}
		}
		return alertExceptions;
	}

	@Override
	public String getName() {
		return "exception-alert";
	}

	private Project queryProjectByDomain(String projectName) {
		Project project = null;
		try {
			project = m_projectDao.findByDomain(projectName, ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return project;
	}

	private TopReport queryTopReport(Date start) {
		String domain = Constants.CAT;
		String date = String.valueOf(start.getTime());
		ModelRequest request = new ModelRequest(domain, start.getTime()).setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean active = true;
		while (active) {
			long current = System.currentTimeMillis();
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("ExceptionAlert", "M" + minuteStr);

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeUtil.ONE_MINUTE * 2));
				Collection<List<Item>> items = topMetric.getError().getResult().values();
				List<Item> item = new ArrayList<Item>();

				if (!items.isEmpty()) {
					item = items.iterator().next();
				}
				Map<String, List<AlertException>> alertExceptions = buildAlertExceptions(item);

				for (Entry<String, List<AlertException>> entry : alertExceptions.entrySet()) {
					try {
						sendAlertForDomain(entry.getKey(), entry.getValue());
					} catch (Exception e) {
						m_logger.error(e.getMessage());
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

	private void sendAlertForDomain(String domain, List<AlertException> exceptions) {
		Project project = queryProjectByDomain(domain);
		List<String> emails = m_alertConfig.buildMailReceivers(project);
		List<String> phones = m_alertConfig.buildSMSReceivers(project);
		List<AlertException> errorExceptions = new ArrayList<AlertException>();
		List<AlertException> warnExceptions = new ArrayList<AlertException>();

		for (AlertException exception : exceptions) {
			if (exception.getAlertFlag() == WARN_FLAG) {
				warnExceptions.add(exception);
			} else if (exception.getAlertFlag() == ERROR_FLAG) {
				errorExceptions.add(exception);
			}
		}

		StringBuilder mailTitle = new StringBuilder();
		String mailContent = buildContent(exceptions.toString(), domain);

		mailTitle.append("[CAT异常告警] [项目: ").append(domain).append("]");
		m_logger.info(mailTitle + " " + mailContent + " " + emails);
		m_mailSms.sendEmail(mailTitle.toString(), mailContent, emails);
		Cat.logEvent("ExceptionAlert", project.getDomain(), Event.SUCCESS, "[邮件告警] " + mailTitle + "  " + mailContent);

		if (!errorExceptions.isEmpty()) {
			String smsContent = buildContent(errorExceptions.toString(), domain);

			m_logger.info(smsContent + " " + phones);
			m_mailSms.sendSms(smsContent, null, phones);
			Cat.logEvent("ExceptionAlert", project.getDomain(), Event.SUCCESS, "[短信告警] " + smsContent);
		}
	}

	private String buildContent(String exceptions, String domain) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT异常告警] [项目: ").append(domain).append("] : ");
		sb.append(exceptions).append("[时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()))
		      .append("]");

		return sb.toString();
	}

	@Override
	public void shutdown() {
	}

	public class AlertException {

		private String m_name;

		private int m_alertFlag;

		private double m_count;

		public AlertException(String name, int alertFlag, double count) {
			m_name = name;
			m_alertFlag = alertFlag;
			m_count = count;
		}

		public int getAlertFlag() {
			return m_alertFlag;
		}

		public String getName() {
			return m_name;
		}

		@Override
		public String toString() {
			return "{exception_name=" + m_name + ", exception_count=" + m_count + "}";
		}
	}
}
