package com.dianping.cat.system.page.alarm;

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
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class RecordManager {

	@Inject
	private AlarmRuleSubscriptionDao m_alarmRuleSubscriptionDao;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private ScheduledReportSubscriptionDao m_scheduledReportSubscriptionDao;

	public void queryAlarmRecordDetail(Payload payload, Model model) {
		int id = payload.getAlarmRecordId();

		try {
			MailRecord record = m_mailRecordDao.findByPK(id, MailRecordEntity.READSET_FULL);
			model.setMailRecord(record);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void queryUserAlarmRecords(Model model, int userId) {
		try {
			List<AlarmRuleSubscription> alarmRuleSubscriptions = m_alarmRuleSubscriptionDao.findByUserId(userId,
			      AlarmRuleSubscriptionEntity.READSET_FULL);

			int size = alarmRuleSubscriptions.size();
			int alarmRuleIds[] = new int[size];

			for (int i = 0; i < size; i++) {
				AlarmRuleSubscription alarmRuleSubscription = alarmRuleSubscriptions.get(i);
				alarmRuleIds[i] = alarmRuleSubscription.getAlarmRuleId();
			}

			List<MailRecord> mails = m_mailRecordDao.findAlarmRecordByRuleId(alarmRuleIds, MailRecordEntity.READSET_FULL);
			model.setMailRecords(mails);
		} catch (DalNotFoundException e) {
		} catch (DalException e) {
			Cat.logError(e);
		}
		model.setTemplateIndex(2);
	}

	public void queryUserReportRecords(Model model, int userId) {
		try {
			List<ScheduledReportSubscription> scheduledReportSubscriptions = m_scheduledReportSubscriptionDao
			      .findByUserId(userId, ScheduledReportSubscriptionEntity.READSET_FULL);
			int size = scheduledReportSubscriptions.size();
			int ruleIds[] = new int[size];

			for (int i = 0; i < size; i++) {
				ScheduledReportSubscription scheduledReportSubscription = scheduledReportSubscriptions.get(i);
				ruleIds[i] = scheduledReportSubscription.getScheduledReportId();
			}
			List<MailRecord> mails = m_mailRecordDao.findReportRecordByRuleId(ruleIds, MailRecordEntity.READSET_FULL);
			model.setMailRecords(mails);
		} catch (DalNotFoundException e) {
		} catch (DalException e) {
			Cat.logError(e);
		}
		model.setTemplateIndex(3);
	}
}
