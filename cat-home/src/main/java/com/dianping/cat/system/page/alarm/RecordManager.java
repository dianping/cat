package com.dianping.cat.system.page.alarm;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscription;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscriptionDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscriptionEntity;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.MailRecordEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscription;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscriptionDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscriptionEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class RecordManager {

	@Inject
	private AlarmRuleSubscriptionDao m_alarmRuleSubscriptionDao;
	
	@Inject
	private ScheduledReportSubscriptionDao m_scheduledReportSubscriptionDao;

	@Inject
	private MailRecordDao m_mailRecordDao;

	public void queryAlarmRecordDetail(Payload payload, Model model) {
		int id = payload.getAlarmRecordId();

		try {
			MailRecord record = m_mailRecordDao.findByPK(id, MailRecordEntity.READSET_FULL);
			model.setMailRecord(record);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
	
	public void queryUserReportRecords(Model model,int userId){
		List<MailRecord> records = new ArrayList<MailRecord>();

		try {
			List<ScheduledReportSubscription> scheduledReportSubscriptions = m_scheduledReportSubscriptionDao.findByUserId(userId, ScheduledReportSubscriptionEntity.READSET_FULL);
			for (ScheduledReportSubscription scheduledReportSubscription : scheduledReportSubscriptions) {
				int scheduledReportId = scheduledReportSubscription.getScheduledReportId();
				
				try {
					List<MailRecord> mails = m_mailRecordDao.findReportRecordByRuleId(scheduledReportId, MailRecordEntity.READSET_FULL);

					for (MailRecord record : mails) {
						records.add(record);
					}
				} catch (DalException e) {
				}
			}
		} catch (DalException e) {
		}
		model.setMailRecords(records);
		model.setTemplateIndex(3);
	}

	public void queryUserAlarmRecords(Model model, int userId) {
		List<MailRecord> records = new ArrayList<MailRecord>();

		try {
			List<AlarmRuleSubscription> alarmRuleSubscriptions = m_alarmRuleSubscriptionDao.findByUserId(userId, AlarmRuleSubscriptionEntity.READSET_FULL);
			for (AlarmRuleSubscription alarmRuleSubscription : alarmRuleSubscriptions) {
				int alarmRuleId = alarmRuleSubscription.getAlarmRuleId();

				try {
					List<MailRecord> mails = m_mailRecordDao.findAlarmRecordByRuleId(alarmRuleId, MailRecordEntity.READSET_FULL);

					for (MailRecord record : mails) {
						records.add(record);
					}
				} catch (DalException e) {
				}
			}
		} catch (DalException e) {
		}
		model.setMailRecords(records);
		model.setTemplateIndex(2);
	}
}
