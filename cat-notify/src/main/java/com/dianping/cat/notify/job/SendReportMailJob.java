package com.dianping.cat.notify.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.notify.config.ConfigContext;
import com.dianping.cat.notify.dao.DailyReportDao;
import com.dianping.cat.notify.dao.MailLogDao;
import com.dianping.cat.notify.dao.SubscriberDao;
import com.dianping.cat.notify.model.DailyReport;
import com.dianping.cat.notify.model.MailLog;
import com.dianping.cat.notify.model.Subscriber;
import com.dianping.cat.notify.report.ReportCreater;
import com.dianping.cat.notify.report.ReportCreaterRegistry;
import com.dianping.cat.notify.server.ContainerHolder;
import com.dianping.cat.notify.util.TimeUtil;
import com.dianping.hawk.common.alarm.service.CommonAlarmService;

public class SendReportMailJob implements ScheduleJob, HandworkJob {
	private final static Logger logger = LoggerFactory.getLogger(SendReportMailJob.class);

	private SubscriberDao m_subscriberDao;

	private CommonAlarmService m_commonService;

	private ReportCreaterRegistry reportCreaterRegistry;

	private MailLogDao m_mailLogDao;

	protected DailyReportDao m_dailyReportDao;

	public static String MAIL_SPLITER = ",";

	private AtomicLong lastDoneTime = new AtomicLong();

	private String m_defaultReceivers;

	@Override
	public boolean init(JobContext jobContext) {
		ContainerHolder holder = (ContainerHolder) jobContext.getData("container");
		ConfigContext configContext = (ConfigContext) jobContext.getData("config");
		if (!reportCreaterRegistry.initReportCreaters(configContext, holder)) {
			return false;
		}
		/* inject dao */
		m_subscriberDao = holder.lookup(SubscriberDao.class, "subscriberDao");
		m_commonService = holder.lookup(CommonAlarmService.class, "commonService");
		m_mailLogDao = holder.lookup(MailLogDao.class, "mailLogDao");
		m_dailyReportDao = holder.lookup(DailyReportDao.class, "dailyReportDao");

		lastDoneTime.set(-1);

		return false;
	}

	@Override
	public void doJob(long timestamp) {
		List<Object> subscriberList = null;
		List<String> domainList = null;
		try {
			subscriberList = m_subscriberDao.getAllMailSubscriber();
		} catch (Exception e) {
			logger.error("fail to get subscribers from database", e);
			return;
		}

		// get all domain
		try {
			domainList = m_dailyReportDao.findDistinctReportDomain(new Date(timestamp - TimeUtil.TWO_DAY_MICROS),
			      new Date(timestamp - TimeUtil.DAY_MICROS), DailyReport.XML_TYPE);
		} catch (Exception e) {
			logger.error("fail to get domain from database", e);
			return;
		}
		if (domainList == null || domainList.size() == 0) {
			return;
		}
		Set<String> domainSet = new HashSet<String>();
		for (String domain : domainList) {
			domainSet.add(domain);
		}

		// send emails to subscribers
		for (Object element : subscriberList) {
			Subscriber subscriber = (Subscriber) element;
			sendBySubscriber(timestamp, false, subscriber);
			// remove the domain
			domainSet.remove(subscriber.getDomain());
		}

		if (domainSet.size() == 0) {
			return;
		}
		Iterator<String> iterator = domainSet.iterator();
		while (iterator.hasNext()) {
			String domain = iterator.next();
			Subscriber subscriber = new Subscriber();
			subscriber.setAddress(m_defaultReceivers);
			subscriber.setDomain(domain);
			// notify administrators to configure the new domain subscriber
			sendBySubscriber(timestamp, false, subscriber);
		}
	}

	private boolean sendBySubscriber(long timestamp, boolean handwork, Subscriber subscriber) {
		Date yestoday = yesterdayZero(new Date(timestamp));
		String dateStr =  TimeUtil.formatTime("yyyy-MM-dd", yestoday);
		String emailTitle = String.format("[CAT] monitor reports of [%s] " + dateStr, subscriber.getDomain());

		List<ReportCreater> reportList = reportCreaterRegistry.getReportCreaters(subscriber.getDomain());
		if (reportList == null) {
			return false;
		}
		boolean sendResult = true;
		for (ReportCreater reportCreater : reportList) {
			if (!handwork) {
				if (!reportCreater.isNeedToCreate(timestamp)) {
					continue;
				}
			}

			String reportContent = reportCreater.createReport(timestamp, subscriber.getDomain());
			if (reportContent.trim().length() == 0) {
				continue;
			}

			MailLog mailLog = new MailLog();
			String address = subscriber.getAddress();
			mailLog.setAddress(address);
			mailLog.setContent(reportContent);
			mailLog.setTitle(emailTitle);
			try {
				if (address == null) {
					logger.error(String.format("subscriber [%s] email is empty", subscriber.getDomain()));
					sendResult = false;
					continue;
				}
				String[] addressArray = address.split(MAIL_SPLITER);
				List<String> addressList = new ArrayList<String>();
				for (int i = 0; i < addressArray.length; i++) {
					addressList.add(addressArray[i]);
				}
				boolean result = m_commonService.sendEmail(reportContent.toString(), emailTitle, addressList);
				if (result) {
					logger.debug("Send Email Success!");
					mailLog.setStatus(MailLog.SEND_SUCCSS);
				} else {
					logger.error(String.format("Send Email fail,time[%s],domain[%s],type[%s],address[%s]", new Date(
					      timestamp), subscriber.getDomain(), subscriber.getType(), subscriber.getAddress()));
					mailLog.setStatus(MailLog.SEND_FAIL);
				}
			} catch (Exception e1) {
				mailLog.setStatus(MailLog.SEND_FAIL);
			}
			try {
				m_mailLogDao.insertMailLog(mailLog);
			} catch (Exception e) {
				logger.error(String.format("Save email report to database fail,time[%s],title[%s],address[%s],content[%s]",
				      new Date(timestamp), mailLog.getTitle(), mailLog.getAddress(), mailLog.getContent()), e);
				continue;
			}
			sendResult = MailLog.SEND_SUCCSS == mailLog.getStatus() ? true : false;
		}
		return sendResult;
	}

	@Override
	public boolean doHandwork(JobContext jobContext) {
		String domain = (String) jobContext.getData("domain");
		long timestamp = (Long) jobContext.getData("day");
		Subscriber subscriber = null;
		try {
			subscriber = m_subscriberDao.getSubscriberByDomain(domain, Subscriber.MAIL);
		} catch (Exception e) {
			logger.error(String.format("fail to get subscriber from databasee. domain[%s]", domain));
		}
		if (subscriber == null) {
			logger.error(String.format("fail to get subscriber from databasee. domain[%s]", domain));
			return false;
		}
		boolean result = sendBySubscriber(timestamp, true, subscriber);
		logger.info(String.format("do send mail handwork. domain[%s] result %s", subscriber.getDomain(),
		      result == true ? "Success" : "Fail"));
		return result;
	}

	@Override
	public boolean isNeedToDo(long timestamp) {
		return true;
	}

	public boolean isNeedToDo_bak(long timestamp) {
		int hour = TimeUtil.getHourOfDay(timestamp);
		/* create report at 00:00:00 */
		if (hour != 0) {
			return false;
		}
		long currentTime = System.currentTimeMillis();
		if (lastDoneTime.get() == -1) {
			/* for first time */
			lastDoneTime.set(currentTime);
			return true;
		}

		long timespan = currentTime - lastDoneTime.get();
		if (timespan > TimeUtil.HOUR_MICROS) {
			/* next time */
			lastDoneTime.set(currentTime);
			return true;
		}
		return false;
	}

	public void setReportCreaterRegistry(ReportCreaterRegistry reportCreaterRegistry) {
		this.reportCreaterRegistry = reportCreaterRegistry;
	}

	public void setDefaultReceivers(String defaultReceivers) {
		this.m_defaultReceivers = defaultReceivers;
	}

	private  Date yesterdayZero(Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
