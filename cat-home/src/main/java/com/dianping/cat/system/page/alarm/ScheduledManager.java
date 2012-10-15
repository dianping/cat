package com.dianping.cat.system.page.alarm;

import java.util.ArrayList;
import java.util.List;

import com.dainping.cat.home.dal.user.DpAdminLogin;
import com.dainping.cat.home.dal.user.DpAdminLoginDao;
import com.dainping.cat.home.dal.user.DpAdminLoginEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscription;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscriptionDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportSubscriptionEntity;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class ScheduledManager {

	@Inject
	private ScheduledReportDao m_scheduledReportDao;
	
	@Inject
	private ScheduledReportSubscriptionDao m_scheduledReportSubscriptionDao;
	
	@Inject
	private DpAdminLoginDao m_loginDao;
	
	public List<ScheduledReport> queryScheduledReports() throws DalException{
		List<ScheduledReport> reports = m_scheduledReportDao.findAll(ScheduledReportEntity.READSET_FULL);
		
		return reports;
	}
	
	public List<String> queryEmailsBySchReportId(int scheduledReportId) throws DalException {
		List<String> emails = new ArrayList<String>();
		List<ScheduledReportSubscription> subscriptions = m_scheduledReportSubscriptionDao.findByScheduledReportId(scheduledReportId,
		      ScheduledReportSubscriptionEntity.READSET_FULL);

		for (ScheduledReportSubscription subscription : subscriptions) {
			DpAdminLogin login = m_loginDao.findByPK(subscription.getUserId(), DpAdminLoginEntity.READSET_FULL);
			emails.add(login.getEmail());
		}
		if (emails.size() == 0) {
			emails.add("yong.you@dianping.com");
		}
		return emails;
	}
	
	public void queryScheduledReports(Model model,int userId) {
		List<UserReportSubState> userRules = new ArrayList<UserReportSubState>();
		try {
			List<ScheduledReport> lists = m_scheduledReportDao.findAll(ScheduledReportEntity.READSET_FULL);
			
			for (ScheduledReport report : lists) {
				int scheduledReportId = report.getId();
				UserReportSubState userSubState = new UserReportSubState(report);

				userRules.add(userSubState);
				try {
					m_scheduledReportSubscriptionDao.findByPK(scheduledReportId, userId, ScheduledReportSubscriptionEntity.READSET_FULL);
					userSubState.setSubscriberState(1);
				} catch (DalNotFoundException nfe) {
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		} catch (DalNotFoundException nfe) {
		} catch (DalException e) {
			Cat.logError(e);
		}
		model.setUserReportSubStates(userRules);
	}


	public void scheduledReportAdd(Payload payload, Model model) {
		List<String> domains = new ArrayList<String>();

		model.setDomains(domains);
	}

	public void scheduledReportAddSubmit(Payload payload, Model model) {
		String domain = payload.getDomain();
		String content = payload.getContent();
		
		ScheduledReport entity = m_scheduledReportDao.createLocal();
		entity.setNames(content);
		entity.setDomain(domain);

		try {
			m_scheduledReportDao.insert(entity);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}

	public void scheduledReportDelete(Payload payload) {
		int id = payload.getScheduledReportId();
		ScheduledReport proto = m_scheduledReportDao.createLocal();

		proto.setKeyId(id);

		try {
			m_scheduledReportDao.deleteByPK(proto);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
	
	public void scheduledReportSub(Payload payload,int loginId) {
		int subState = payload.getUserSubState();
		int scheduledReportId = payload.getScheduledReportId();

		ScheduledReportSubscription scheduledReportSubscription = m_scheduledReportSubscriptionDao.createLocal();

		scheduledReportSubscription.setKeyScheduledReportId(scheduledReportId);
		scheduledReportSubscription.setKeyUserId(loginId);
		scheduledReportSubscription.setUserId(loginId);
		scheduledReportSubscription.setScheduledReportId(scheduledReportId);

		try {
			if (subState == 1) {
				m_scheduledReportSubscriptionDao.deleteByPK(scheduledReportSubscription);
			} else {
				m_scheduledReportSubscriptionDao.insert(scheduledReportSubscription);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void scheduledReportUpdate(Payload payload, Model model) {
		int id = payload.getScheduledReportId();

		try {
			ScheduledReport scheduledReport = m_scheduledReportDao.findByPK(id, ScheduledReportEntity.READSET_FULL);
			model.setScheduledReport(scheduledReport);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
	public void scheduledReportUpdateSubmit(Payload payload, Model model) {
		int id = payload.getScheduledReportId();
		String content = payload.getContent();
		ScheduledReport entity = m_scheduledReportDao.createLocal();

		entity.setNames(content);
		entity.setKeyId(id);
		try {
			m_scheduledReportDao.updateByPK(entity, ScheduledReportEntity.UPDATESET_UPDATE_REPORTS);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}
}
