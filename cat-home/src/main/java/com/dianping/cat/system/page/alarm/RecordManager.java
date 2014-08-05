package com.dianping.cat.system.page.alarm;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.MailRecordEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscription2;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscription2Dao;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscription2Entity;

public class RecordManager {

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private ScheduledReportSubscription2Dao m_scheduledReportSubscriptionDao;

	public void queryAlarmRecordDetail(Payload payload, Model model) {
		int id = payload.getAlarmRecordId();

		try {
			MailRecord record = m_mailRecordDao.findByPK(id, MailRecordEntity.READSET_FULL);
			model.setMailRecord(record);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void queryUserReportRecords(Model model, String userName) {
		try {
			List<ScheduledReportSubscription2> scheduledReportSubscriptions = m_scheduledReportSubscriptionDao
			      .findByUserName(userName, ScheduledReportSubscription2Entity.READSET_FULL);
			int size = scheduledReportSubscriptions.size();
			int ruleIds[] = new int[size];

			for (int i = 0; i < size; i++) {
				ScheduledReportSubscription2 scheduledReportSubscription = scheduledReportSubscriptions.get(i);
				ruleIds[i] = scheduledReportSubscription.getScheduledReportId();
			}
			List<MailRecord> mails = m_mailRecordDao.findReportRecordByRuleId(ruleIds,
			      MailRecordEntity.READSET_ALL_EXCLUDE_CONTENT);
			model.setMailRecords(mails);
		} catch (DalNotFoundException e) {
		} catch (DalException e) {
			Cat.logError(e);
		}
		model.setTemplateIndex(3);
	}
}
