package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.top.TopMetric;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.report.task.alert.exception.AlertExceptionBuilder.AlertException;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public class ExceptionAlert implements Task, LogEnabled {

	@Inject
	protected AlertDao m_alertDao;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private ExceptionAlertConfig m_alertConfig;

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject
	private AlertExceptionBuilder m_alertBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int ALERT_PERIOD = 1;

	private Logger m_logger;

	private Alert buildAlert(String domainName, AlertException exception, String mailContent) {
		Alert alert = new Alert();
		
		alert.setDomain(domainName);
		alert.setAlertTime(new Date());
		alert.setCategory(getName());
		alert.setType(exception.getType());
		alert.setContent(mailContent);
		alert.setMetric(exception.getName());
		
	   return alert;
   }

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
				Map<String, List<AlertException>> alertExceptions = m_alertBuilder.buildAlertExceptions(item);

				for (Entry<String, List<AlertException>> entry : alertExceptions.entrySet()) {
					try {
						sendAndStoreAlert(entry.getKey(), entry.getValue());
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

	private void sendAndStoreAlert(String domain, List<AlertException> exceptions) {
		Project project = queryProjectByDomain(domain);
		List<String> emails = m_alertConfig.buildMailReceivers(project);
		List<String> phones = m_alertConfig.buildSMSReceivers(project);
		String mailTitle = m_alertConfig.buildMailTitle(domain, null);
		String mailContent = m_alertBuilder.buildMailContent(exceptions.toString(), domain);

		m_mailSms.sendEmail(mailTitle, mailContent, emails);
		m_logger.info(mailTitle + " " + mailContent + " " + emails);
		Cat.logEvent("ExceptionAlert", domain, Event.SUCCESS, "[邮件告警] " + mailTitle + "  " + mailContent);

		storeAlerts(domain, exceptions, mailTitle + "<br/>" + mailContent);

		List<AlertException> errorExceptions = m_alertBuilder.buildErrorException(exceptions);

		if (!errorExceptions.isEmpty()) {
			String smsContent = m_alertBuilder.buildContent(errorExceptions.toString(), domain);

			m_mailSms.sendSms(smsContent, smsContent, phones);
			m_logger.info(smsContent + " " + phones);
			Cat.logEvent("ExceptionAlert", domain, Event.SUCCESS, "[短信告警] " + smsContent);
		}
	}

	private void storeAlerts(String domain, List<AlertException> exceptions, String mailContent) {
		for (AlertException exception : exceptions) {
			storeAlert(domain, exception, mailContent);
		}
	}

	private void storeAlert(String domainName, AlertException exception, String mailContent) {
		Alert alert = buildAlert(domainName, exception, mailContent);

		try {
			int count = m_alertDao.insert(alert);

			if (count != 1) {
				Cat.logError("insert alert error: " + alert.toString(), new RuntimeException());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void shutdown() {
	}
}
