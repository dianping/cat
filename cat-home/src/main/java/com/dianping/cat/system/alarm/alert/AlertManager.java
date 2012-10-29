package com.dianping.cat.system.alarm.alert;

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

	public void addAlarmInfo(AlertInfo info) {
		m_alarmInfos.offer(info);
	}

	@Override
	public void initialize() throws InitializationException {
		SendAlarmTask sendAlarmTask = new SendAlarmTask();

		Threads.forGroup("Cat").start(sendAlarmTask);
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

	public class SendAlarmTask implements Task, Initializable {

		@Override
		public String getName() {
			return "Send-Notifycation";
		}

		@Override
		public void initialize() throws InitializationException {

		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					AlertInfo entity = m_alarmInfos.poll(5, TimeUnit.MILLISECONDS);

					if (entity != null) {
						Transaction t = Cat.newTransaction("System", "Email");
						String alarmType = entity.getRuleType();
						boolean sendResult = false;

						if (alarmType.equals(AlertInfo.EXCEPTION)) {
							insert(entity, 2, sendResult);
						} else if (alarmType.equals(AlertInfo.SERVICE)) {
							insert(entity, 3, sendResult);
						}

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
