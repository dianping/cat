package com.dianping.cat.system.alarm.alert;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.message.Transaction;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class AlertManager implements Initializable {

	private final BlockingQueue<AlertInfo> m_alarmInfos = new LinkedBlockingQueue<AlertInfo>(1000);

	@Inject
	private MailRecordDao m_mailRecordDao;

	public void addAlarmInfo(int type, String title, String content, List<String> address, int ruleId, Date date) {
		AlertInfo info = new AlertInfo();

		info.setContent(content);
		info.setTitle(title);
		info.setType(type);
		info.setRuleId(ruleId);
		info.setDate(date);
		if (type == AlertInfo.EMAIL_TYPE) {
			info.setMails(address);
		} else {
			info.setPhones(address);
		}
		m_alarmInfos.offer(info);
	}

	@Override
	public void initialize() throws InitializationException {
		SendAlarmTask sendAlarmTask = new SendAlarmTask();

		Threads.forGroup("Cat-Alarm").start(sendAlarmTask);
	}

	private void insert(AlertInfo info, int status) {
		MailRecord record = m_mailRecordDao.createLocal();

		record.setContent(info.getContent());
		record.setTitle(info.getTitle());
		record.setRuleId(info.getRuleId());
		record.setReceivers(info.getMails().toString());
		record.setStatus(status);
		record.setType(1);// for alarm type
		try {
			m_mailRecordDao.insert(record);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public class SendAlarmTask implements Task {

		@Override
		public String getName() {
			return "Send-Alarm";
		}

		@Override
		public void run() {
			boolean active = true;
			
			while (active) {
				try {
					AlertInfo entity = m_alarmInfos.poll(5, TimeUnit.MILLISECONDS);

					if (entity != null) {
						Transaction t = Cat.newTransaction("System", "Email");
						// Send Email
						// boolean result
						insert(entity, 1);
						t.addData(entity.getContent() + entity.getTitle());
						t.setStatus(Transaction.SUCCESS);
						t.complete();
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
