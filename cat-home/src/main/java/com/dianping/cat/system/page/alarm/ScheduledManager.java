package com.dianping.cat.system.page.alarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportEntity;
import com.dianping.cat.home.dal.alarm.ScheduledSubscription;
import com.dianping.cat.home.dal.alarm.ScheduledSubscriptionDao;
import com.dianping.cat.home.dal.alarm.ScheduledSubscriptionEntity;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.alarm.UserReportSubState.UserReportSubStateCompartor;
import com.site.lookup.util.StringUtils;

public class ScheduledManager  {

	@Inject
	private ScheduledReportDao m_scheduledReportDao;

	@Inject
	private ScheduledSubscriptionDao m_scheduledReportSubscriptionDao;

	@Inject
	private ProjectService m_projectService;

	private Map<String, ScheduledReport> m_reports = new HashMap<String, ScheduledReport>();

	public List<String> queryEmailsBySchReportId(int scheduledReportId) throws DalException {
		List<String> emails = new ArrayList<String>();
		List<ScheduledSubscription> subscriptions = m_scheduledReportSubscriptionDao.findByScheduledReportId(
		      scheduledReportId, ScheduledSubscriptionEntity.READSET_FULL);

		for (ScheduledSubscription subscription : subscriptions) {
			emails.add(subscription.getUserName() + "@dianping.com");
		}
		return emails;
	}

	public Collection<ScheduledReport> queryScheduledReports() {
		return m_reports.values();
	}

	public void queryScheduledReports(Model model, String userName) {
		List<UserReportSubState> userRules = new ArrayList<UserReportSubState>();
		try {
			Collection<ScheduledReport> lists = m_reports.values();

			for (ScheduledReport report : lists) {
				int scheduledReportId = report.getId();
				UserReportSubState userSubState = new UserReportSubState(report);

				userRules.add(userSubState);
				try {
					m_scheduledReportSubscriptionDao.findByPK(scheduledReportId, userName,
					      ScheduledSubscriptionEntity.READSET_FULL);
					userSubState.setSubscriberState(1);
				} catch (DalNotFoundException nfe) {
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		Collections.sort(userRules, new UserReportSubStateCompartor());
		model.setUserReportSubStates(userRules);
	}

	public void refreshScheduledReport() throws Exception {
		Map<String, Project> projects = m_projectService.findAllProjects();

		for (Entry<String, Project> entry : projects.entrySet()) {
			String domain = entry.getKey();
			String cmdbDomain = entry.getValue().getCmdbDomain();

			if (StringUtils.isNotEmpty(cmdbDomain) && !m_reports.containsKey(cmdbDomain)) {
				updateData(cmdbDomain);
			} else if (StringUtils.isEmpty(cmdbDomain) && !m_reports.containsKey(domain)) {
				updateData(domain);
			}
		}
	}

	public void scheduledReportDelete(Payload payload) {
		int id = payload.getScheduledReportId();
		ScheduledReport proto = m_scheduledReportDao.createLocal();

		proto.setKeyId(id);

		try {
			ScheduledReport report = m_scheduledReportDao.findByPK(id, ScheduledReportEntity.READSET_FULL);
			String domain = report.getDomain();
			m_scheduledReportDao.deleteByPK(proto);

			if (StringUtils.isNotEmpty(domain)) {
				m_reports.remove(domain);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public boolean scheduledReportSub(Payload payload, String userName) {
		int subState = payload.getUserSubState();
		int scheduledReportId = payload.getScheduledReportId();

		ScheduledSubscription scheduledReportSubscription = m_scheduledReportSubscriptionDao.createLocal();

		scheduledReportSubscription.setKeyScheduledReportId(scheduledReportId);
		scheduledReportSubscription.setKeyUserName(userName);
		scheduledReportSubscription.setUserName(userName);
		scheduledReportSubscription.setScheduledReportId(scheduledReportId);

		try {
			if (subState == 1) {
				m_scheduledReportSubscriptionDao.deleteByPK(scheduledReportSubscription);
			} else {
				m_scheduledReportSubscriptionDao.insert(scheduledReportSubscription);
			}
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
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

	private void updateData(String domain) throws Exception {
		ScheduledReport entity = m_scheduledReportDao.createLocal();

		entity.setNames("transaction;event;problem;health");
		entity.setDomain(domain);
		m_scheduledReportDao.insert(entity);

		ScheduledReport report = m_scheduledReportDao.findByDomain(domain, ScheduledReportEntity.READSET_FULL);
		m_reports.put(domain, report);
	}

	public class ScheduledReportUpdateTask implements Task {

		@Override
		public String getName() {
			return "ScheduledReport-Domain-Update";
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					refreshScheduledReport();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(TimeHelper.ONE_MINUTE);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
