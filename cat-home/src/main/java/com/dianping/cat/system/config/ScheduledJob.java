package com.dianping.cat.system.config;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.*;
import com.dianping.cat.home.dal.user.DpAdminLogin;
import com.dianping.cat.home.dal.user.DpAdminLoginDao;
import com.dianping.cat.home.dal.user.DpAdminLoginEntity;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

public class ScheduledJob implements Initializable {

	@Inject
	private ScheduledReportDao m_scheduledReportDao;

	@Inject
	private ScheduledReportSubscriptionDao m_scheduledReportSubscriptionDao;

	@Inject
	private ScheduledSubscriptionDao m_scheduledSubscriptionDao;

	@Inject
	private DpAdminLoginDao m_loginDao;

	@Override
	public void initialize() {
		List<ScheduledReport> reports = queryScheduledReports();
		List<String> userNames = null;

		for (ScheduledReport report : reports) {
			int scheduledReportId = report.getId();

			try {
				userNames = queryUserNamesBySchReportId(scheduledReportId);
			} catch (DalException e) {
				Cat.logError(e);
			}

			if (userNames != null) {
				for (String userName : userNames) {
					insertSchReportIdAndUserName(scheduledReportId, userName);
				}
			}
		}
	}

	public List<String> queryUserNamesBySchReportId(int scheduledReportId) throws DalException {
		List<String> userNames = new ArrayList<String>();
		List<ScheduledReportSubscription> subscriptions = m_scheduledReportSubscriptionDao.findByScheduledReportId(
		      scheduledReportId, ScheduledReportSubscriptionEntity.READSET_FULL);

		for (ScheduledReportSubscription subscription : subscriptions) {
			try {
				DpAdminLogin login = m_loginDao.findByPK(subscription.getUserId(), DpAdminLoginEntity.READSET_FULL);
				String email = login.getEmail();
				String userName = email.substring(0, email.indexOf('@'));

				userNames.add(userName);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return userNames;
	}

	public List<ScheduledReport> queryScheduledReports() {
		try {
			List<ScheduledReport> reports = m_scheduledReportDao.findAll(ScheduledReportEntity.READSET_FULL);

			return reports;
		} catch (Exception e) {
			return new ArrayList<ScheduledReport>();
		}
	}

	public void insertSchReportIdAndUserName(int scheduledReportId, String userName) {
		ScheduledSubscription subscription = new ScheduledSubscription();
		subscription.setScheduledReportId(scheduledReportId);
		subscription.setUserName(userName);
		subscription.setKeyScheduledReportId(scheduledReportId);
		subscription.setKeyUserName(userName);

		try {
			m_scheduledSubscriptionDao.insertIngore(subscription);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
}
