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

public class DatabaseConfigManager implements Initializable {

    @Inject
    private ScheduledReportDao m_scheduledReportDao;

    @Inject
    private ScheduledReportSubscriptionDao m_scheduledReportSubscriptionDao;

    @Inject
    private ScheduledReportSubscription2Dao m_scheduledReportSubscription2Dao;

    @Inject
    private DpAdminLoginDao m_loginDao;

    @Override
    public void initialize () {
        List<ScheduledReport> reports = null;
        List<String> userNames = null;

        try {
            reports = queryScheduledReports();
        } catch (DalException e) {
            Cat.logError(e);
        }

        for (ScheduledReport report : reports) {
            int scheduledReportId = report.getId();

            try {
                userNames = queryUserNamesBySchReportId(scheduledReportId);
            } catch (DalException e) {
                Cat.logError(e);
            }

            for (String userName : userNames) {
                insertSchReportIdAndUserName(scheduledReportId, userName);
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

    public List<ScheduledReport> queryScheduledReports() throws DalException {
		List<ScheduledReport> reports = m_scheduledReportDao.findAll(ScheduledReportEntity.READSET_FULL);

		return reports;
	}

    public void insertSchReportIdAndUserName (int scheduledReportId, String userName) {
        ScheduledReportSubscription2 subscription2 = new ScheduledReportSubscription2();
        subscription2.setScheduledReportId(scheduledReportId);
        subscription2.setUserName(userName);
        subscription2.setKeyScheduledReportId(scheduledReportId);
        subscription2.setKeyUserName(userName);

        try {
            m_scheduledReportSubscription2Dao.insert(subscription2);
		} catch (DalException e) {
			Cat.logError(e);
		}
    }
}

