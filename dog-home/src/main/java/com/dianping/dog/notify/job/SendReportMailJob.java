package com.dianping.dog.notify.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.dog.dal.Dailyreport;
import com.dianping.dog.dal.DailyreportDao;
import com.dianping.dog.dal.DailyreportEntity;
import com.dianping.dog.dal.Maillog;
import com.dianping.dog.dal.MaillogDao;
import com.dianping.dog.dal.Subscriber;
import com.dianping.dog.dal.SubscriberDao;
import com.dianping.dog.dal.SubscriberEntity;
import com.dianping.dog.notify.config.ConfigContext;
import com.dianping.dog.notify.report.DefaultContainerHolder;
import com.dianping.dog.notify.report.ReportConstants;
import com.dianping.dog.notify.report.ReportCreater;
import com.dianping.dog.notify.report.ReportCreaterRegistry;
import com.dianping.dog.service.CommonService;
import com.dianping.dog.util.TimeUtil;
import com.site.lookup.annotation.Inject;

public class SendReportMailJob implements ScheduleJob, HandworkJob, LogEnabled{
	
	private SubscriberDao m_subscriberDao;

	private CommonService m_commonService;

	@Inject
	private ReportCreaterRegistry m_reportCreaterRegistry;

	private MaillogDao m_mailLogDao;

	protected DailyreportDao m_dailyReportDao;

	public static String MAIL_SPLITER = ",";

	private AtomicLong lastDoneTime = new AtomicLong();

	@Inject
	private String m_defaultReceivers;
	
	private Logger m_logger;

	@Override
	public boolean init(JobContext jobContext) {
		DefaultContainerHolder holder = (DefaultContainerHolder) jobContext.getData("container");
		ConfigContext configContext = (ConfigContext) jobContext.getData("config");
		if (!m_reportCreaterRegistry.initReportCreaters(configContext, holder)) {
			return false;
		}
		/* inject dao */
		m_subscriberDao = holder.lookup(SubscriberDao.class);
		m_commonService = holder.lookup(CommonService.class);
		m_mailLogDao = holder.lookup(MaillogDao.class);
		m_dailyReportDao = holder.lookup(DailyreportDao.class);

		lastDoneTime.set(-1);

		return false;
	}

	@Override
	public void doJob(long timestamp) {
		List<Subscriber> subscriberList = null;
		List<Dailyreport> domainList = null;
		try {
			subscriberList = m_subscriberDao.findAllSubscriber(SubscriberEntity.READSET_FULL);
		} catch (Exception e) {
			m_logger.error("fail to get subscribers from database", e);
			return;
		}

		// get all domain
		try {
			domainList = m_dailyReportDao.findDistinctReportDomain(new Date(timestamp - TimeUtil.TWO_DAY_MICROS),
			      new Date(timestamp - TimeUtil.DAY_MICROS), ReportConstants.XML_TYPE, DailyreportEntity.READSET_FULL);
		} catch (Exception e) {
			m_logger.error("fail to get domain from database", e);
			return;
		}
		if (domainList == null || domainList.size() == 0) {
			return;
		}
		Set<String> domainSet = new HashSet<String>();
		for (Dailyreport report : domainList) {
			domainSet.add(report.getDomain());
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
		String dateStr = TimeUtil.formatTime("yyyy-MM-dd", yestoday);
		String emailTitle = String.format("[CAT] monitor reports of [%s] " + dateStr, subscriber.getDomain());

		List<ReportCreater> reportList = m_reportCreaterRegistry.getReportCreaters(subscriber.getDomain());
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
			String reportContent = "";
			try {
				reportContent = reportCreater.createReport(timestamp, subscriber.getDomain());
			} catch (Exception e1) {
				m_logger.debug(String.format("fail to create Report for domain [%s]", subscriber.getDomain()));
			}
			if (reportContent.trim().length() == 0) {
				continue;
			}

			Maillog mailLog = new Maillog();
			String address = subscriber.getAddress();
			mailLog.setAddress(address);
			mailLog.setContent(reportContent);
			mailLog.setTitle(emailTitle);
			try {
				if (address == null) {
					m_logger.error(String.format("subscriber [%s] email is empty", subscriber.getDomain()));
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
					m_logger.debug("Send Email Success!");
					mailLog.setStatus(ReportConstants.SEND_SUCCSS);
				} else {
					m_logger.error(String.format("Send Email fail,time[%s],domain[%s],type[%s],address[%s]", new Date(
					      timestamp), subscriber.getDomain(), subscriber.getType(), subscriber.getAddress()));
					mailLog.setStatus(ReportConstants.SEND_FAIL);
				}
			} catch (Exception e1) {
				mailLog.setStatus(ReportConstants.SEND_FAIL);
			}
			try {
				m_mailLogDao.insert(mailLog);
			} catch (Exception e) {
				m_logger.error(String.format("Save email report to database fail,time[%s],title[%s],address[%s],content[%s]",
				      new Date(timestamp), mailLog.getTitle(), mailLog.getAddress(), mailLog.getContent()), e);
				continue;
			}
			sendResult = ReportConstants.SEND_SUCCSS == mailLog.getStatus() ? true : false;
		}
		return sendResult;
	}

	@Override
	public boolean doHandwork(JobContext jobContext) {
		String domain = (String) jobContext.getData("domain");
		long timestamp = (Long) jobContext.getData("day");
		List<Subscriber> subscriberList = null;
		try {
			subscriberList = m_subscriberDao.findAllSubscriberByDomain(domain, ReportConstants.MAIL,
			      SubscriberEntity.READSET_FULL);
		} catch (Exception e) {
			m_logger.error(String.format("fail to get subscriber from databasee. domain[%s]", domain));
		}
		if (subscriberList == null) {
			m_logger.error(String.format("fail to get subscriber from databasee. domain[%s]", domain));
			return false;
		}
		Subscriber subscriber = subscriberList.get(0);
		boolean result = sendBySubscriber(timestamp, true, subscriber);
		m_logger.info(String.format("do send mail handwork. domain[%s] result %s", subscriber.getDomain(),
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
		this.m_reportCreaterRegistry = reportCreaterRegistry;
	}

	public void setDefaultReceivers(String defaultReceivers) {
		this.m_defaultReceivers = defaultReceivers;
	}

	private Date yesterdayZero(Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Override
   public void enableLogging(Logger logger) {
      this.m_logger = logger;
   }
}
