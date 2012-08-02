package com.dianping.cat.notify.job;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.dianping.cat.notify.config.ConfigContext;
import com.dianping.cat.notify.dao.MailLogDao;
import com.dianping.cat.notify.dao.SubscriberDao;
import com.dianping.cat.notify.model.MailLog;
import com.dianping.cat.notify.model.Subscriber;
import com.dianping.cat.notify.model.entity.Report;
import com.dianping.cat.notify.model.entity.ScheduleReports;
import com.dianping.cat.notify.model.transform.DefaultDomParser;
import com.dianping.cat.notify.report.ReportCreater;
import com.dianping.cat.notify.server.ContainerHolder;
import com.dianping.cat.notify.util.TimeUtil;
import com.dianping.hawk.common.alarm.service.CommonAlarmService;
import com.site.helper.Files;

public class SendReportMailJob implements ScheduleJob, HandworkJob {
	private final static Logger logger = LoggerFactory.getLogger(SendReportMailJob.class);

	private Map<String, List<ReportCreater>> m_reportCreaters;// domain=>ReportCreater列表

	private SubscriberDao m_subscriberDao;

	private CommonAlarmService m_commonService;

	private MailLogDao m_mailLogDao;

	public static String MAIL_SPLITER = ",";

	private AtomicLong lastDoneTime = new AtomicLong();

	@Override
	public boolean init(JobContext jobContext) {
		try {
			ContainerHolder holder = (ContainerHolder) jobContext.getData("container");
			ConfigContext configContext = (ConfigContext) jobContext.getData("config");
			String configPath = configContext.getProperty("schedulejob.config.path");

			m_reportCreaters = new HashMap<String, List<ReportCreater>>();
			DefaultDomParser parser = new DefaultDomParser();
			String source = Files.forIO().readFrom(new FileInputStream(configPath), "utf-8");
			ScheduleReports schedule_Reports = parser.parse(source);
			List<Report> reports = schedule_Reports.getReports();

			ClassLoader loader = getClass().getClassLoader();
			for (Report report : reports) {
				String className = report.getCreateClass();
				Class<?> createClass = loader.loadClass(className);
				ReportCreater reportCreater = (ReportCreater) createClass.newInstance();
				if (!reportCreater.init(report, holder)) {
					logger.error("fail to init the report create:", report.toString());
					continue;
				}
				List<ReportCreater> reportList = m_reportCreaters.get(report.getDomain());
				if (reportList == null) {
					reportList = new ArrayList<ReportCreater>();
					m_reportCreaters.put(report.getDomain(), reportList);
				}
				reportList.add(reportCreater);
			}
			/* inject dao */
			m_subscriberDao = holder.lookup(SubscriberDao.class, "subscriberDao");
			m_commonService = holder.lookup(CommonAlarmService.class, "commonService");
			m_mailLogDao = holder.lookup(MailLogDao.class, "mailLogDao");

			lastDoneTime.set(-1);
			return true;
		} catch (IOException e) {
			logger.error("fail to read the config file.", e);
		} catch (SAXException e) {
			logger.error("fail to parse the config file.", e);
		} catch (ClassNotFoundException e) {
			logger.error("fail to load the report create class.", e);
		} catch (IllegalAccessException e) {
			logger.error("have not privalleage to load the report create class.", e);
		} catch (InstantiationException e) {
			logger.error("fail to  instantiation the report create class.", e);
		}
		return false;
	}

	@Override
	public void doJob(long timestamp) {
		List<Object> subscriberList = null;
		try {
			subscriberList = m_subscriberDao.getAllMailSubscriber();
		} catch (Exception e) {
			logger.error("fail to get subscribers from database", e);
			return;
		}
		for (Object element : subscriberList) {
			Subscriber subscriber = (Subscriber) element;
			sendBySubscriber(timestamp, false, subscriber);
		}
	}

	private boolean sendBySubscriber(long timestamp, boolean handwork, Subscriber subscriber) {
		String emailTitle = String.format("Cat monitor reports of [%s]", subscriber.getDomain());
		StringBuilder reportContent = new StringBuilder();
		List<ReportCreater> reportList = m_reportCreaters.get(subscriber.getDomain());
		for (ReportCreater reportCreater : reportList) {
			if (!handwork) {
				if (!reportCreater.isNeedToCreate(timestamp)) {
					continue;
				}
			}
			reportContent.append(reportCreater.createReport(timestamp, subscriber.getDomain()));
		}
		if (reportContent.toString().trim().length() == 0) {
			return false;
		}
		MailLog mailLog = new MailLog();
		String address = subscriber.getAddress();
		mailLog.setAddress(address);
		mailLog.setContent(reportContent.toString());
		mailLog.setTitle(emailTitle);
		try {
			if (address == null) {
				logger.error(String.format("subscriber [%s] email is empty", subscriber.getDomain()));
				return false;
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
				logger.error(String.format("Send Email fail,time[%s],domain[%s],type[%s],address[%d]", new Date(timestamp),
				      subscriber.getDomain(), subscriber.getType(), subscriber.getAddress()));
				mailLog.setStatus(MailLog.SEND_FAIL);
			}
		} catch (Exception e1) {
			// logger.error(String.format("Send Email fail,time[%s],domain[%s],type[%s],address[%d]",
			// new Date(timestamp),subscriber.getDomain(), subscriber.getType(),
			// subscriber.getAddress()));
			mailLog.setStatus(MailLog.SEND_FAIL);
		}
		try {
			m_mailLogDao.insertMailLog(mailLog);
		} catch (Exception e) {
			logger.error(String.format("Save email report to database fail,time[%s],title[%s],address[%s],content[%d]",
			      new Date(timestamp), mailLog.getTitle(), mailLog.getAddress(), mailLog.getContent()), e);
		}
		return MailLog.SEND_SUCCSS == mailLog.getStatus() ? true : false;
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

}
