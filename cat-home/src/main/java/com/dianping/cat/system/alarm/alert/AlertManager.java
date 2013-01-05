package com.dianping.cat.system.alarm.alert;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.system.tool.MailSMS;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

public class AlertManager implements Initializable {

	private final BlockingQueue<AlertInfo> m_alarmInfos = new LinkedBlockingQueue<AlertInfo>(1000);

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private MailSMS m_mailSms;

	public void addAlarmInfo(AlertInfo info) {
		m_alarmInfos.offer(info);
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_configManager.isJobMachine() && !m_configManager.isLocalMode()) {
			SendAlarmTask sendAlarmTask = new SendAlarmTask();

			Threads.forGroup("Cat").start(sendAlarmTask);
		}
	}

	private void insert(AlertInfo info, int type, boolean sendResult) {
		MailRecord record = m_mailRecordDao.createLocal();

		record.setContent(info.getContent());
		record.setTitle(info.getTitle());
		record.setRuleId(info.getRuleId());
		record.setReceivers(info.getMails().toString());

		if (sendResult) {
			record.setStatus(1);
		} else {
			record.setStatus(0);
		}
		record.setType(type);// for alarm type

		try {
			m_mailRecordDao.insert(record);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public class SendAlarmTask implements Task {

		@Override
		public String getName() {
			return "Send-Notifycation";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					AlertInfo entity = m_alarmInfos.poll(5, TimeUnit.MILLISECONDS);

					if (entity != null) {
						String alarmType = entity.getRuleType();

						int alertType = entity.getAlertType();
						String title = entity.getTitle();
						String content = entity.getContent();
						boolean sendResult = false;

						if (alertType == AlertInfo.EMAIL_TYPE) {
							List<String> mails = entity.getMails();

							sendResult = m_mailSms.sendEmail(title, content, mails);
						} else if (alertType == AlertInfo.SMS_TYPE) {
							List<String> phones = entity.getPhones();

							sendResult = m_mailSms.sendSMS(title + " " + content, phones);
						}

						if (alarmType.equals(AlertInfo.EXCEPTION)) {
							insert(entity, 2, sendResult);
						} else if (alarmType.equals(AlertInfo.SERVICE)) {
							insert(entity, 3, sendResult);
						}
					} else {
						try {
							Thread.sleep(2 * 1000);
						} catch (InterruptedException e) {
							active = false;
						}
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
