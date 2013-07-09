package com.dianping.cat.system.alarm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.alarm.AlarmRule;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleEntity;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportEntity;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class AlarmRuleCreator implements Task {

	@Inject
	private AlarmRuleDao m_alarmRuleDao;

	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;

	@Inject
	private ScheduledReportDao m_scheduledReportDao;

	@Inject
	private ModelService<EventReport> m_service;

	/**
	 * Get all domains from realtime
	 * 
	 * @return
	 * @throws DalException
	 */
	private Set<String> getAllDomains() throws DalException {
		String domain = CatString.CAT;
		ModelRequest request = new ModelRequest(domain, ModelPeriod.CURRENT.getStartTime())//
		      .setProperty("ip", CatString.ALL);

		if (m_service.isEligable(request)) {
			ModelResponse<EventReport> response = m_service.invoke(request);
			EventReport report = response.getModel();

			if (report != null) {
				return report.getDomainNames();
			} else {
				return new HashSet<String>();
			}
		} else {
			throw new RuntimeException("Internal error: no eligable event service registered for " + request + "!");
		}
	}

	@Override
	public String getName() {
		return "Default-Rule-Creator";
	}

	private void insertAlarmRule(int templateId, String domain) throws DalException {
		AlarmRule entity = m_alarmRuleDao.createLocal();

		entity.setTemplateId(templateId);
		entity.setDomain(domain);
		entity.setContent("");
		m_alarmRuleDao.insert(entity);
	}

	private void insertScheduled(String domain) throws DalException {
		ScheduledReport entity = m_scheduledReportDao.createLocal();

		entity.setDomain(domain);
		entity.setNames("transaction;event;problem;health");
		m_scheduledReportDao.insert(entity);
	}

	private AlarmTemplate queryTemplateByName(String name) {
		try {
			AlarmTemplate entity = m_alarmTemplateDao.findAlarmTemplateByName(name, AlarmTemplateEntity.READSET_FULL);

			return entity;
		} catch (DalNotFoundException nfe) {
		} catch (DalException e) {
			Cat.logError(e);
		}

		throw new RuntimeException("Template Can't be null!");
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e1) {
		}
		boolean active = true;

		while (active) {
			try {
				int exceptionTemplateId = queryTemplateByName("exception").getId();
				int serviceTemplateId = queryTemplateByName("service").getId();
				List<AlarmRule> exceptionAlarmRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(exceptionTemplateId,
				      AlarmRuleEntity.READSET_FULL);
				List<AlarmRule> serviceAlarmRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(serviceTemplateId,
				      AlarmRuleEntity.READSET_FULL);
				List<ScheduledReport> scheduledReports = m_scheduledReportDao.findAll(ScheduledReportEntity.READSET_FULL);
				Set<String> allExceptionDomains = getAllDomains();
				Set<String> allServiceDomains = new HashSet<String>(allExceptionDomains);
				Set<String> allScheduledDomains = new HashSet<String>(allExceptionDomains);

				for (AlarmRule temp : exceptionAlarmRules) {
					allExceptionDomains.remove(temp.getDomain());
				}
				for (String domain : allExceptionDomains) {
					Transaction t = Cat.newTransaction("Alarm", "ExceptionRuleAdd");
					try {
						insertAlarmRule(exceptionTemplateId, domain);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
					}
					t.complete();
				}

				for (AlarmRule temp : serviceAlarmRules) {
					allServiceDomains.remove(temp.getDomain());
				}

				for (String domain : allServiceDomains) {
					Transaction t = Cat.newTransaction("Alarm", "ServiceRuleAdd");
					try {
						insertAlarmRule(serviceTemplateId, domain);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
					}
					t.complete();
				}

				for (ScheduledReport temp : scheduledReports) {
					allScheduledDomains.remove(temp.getDomain());
				}

				for (String domain : allScheduledDomains) {
					Transaction t = Cat.newTransaction("Alarm", "ScheduledReportAdd");
					try {
						insertScheduled(domain);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
					}
					t.complete();
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(10 * 60 * 1000);
			} catch (Exception e) {
				active = false;
			}
		}

	}

	@Override
	public void shutdown() {
	}

}
