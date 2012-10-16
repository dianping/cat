package com.dianping.cat.system.page.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.AlarmRule;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleEntity;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscription;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscriptionDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleSubscriptionEntity;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateEntity;
import com.dianping.cat.system.page.alarm.UserAlarmSubState.UserAlarmSubStateCompartor;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class RuleManager {
	
	@Inject
	private AlarmRuleDao m_alarmRuleDao;
	
	@Inject
	private AlarmRuleSubscriptionDao m_alarmRuleSubscriptionDao;
	
	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;
	
	public void queryExceptionRuleList(Model model,int userId) {
		List<UserAlarmSubState> userRules = new ArrayList<UserAlarmSubState>();
		try {
			int templateId = queryTemplateByName("exception").getId();
			List<AlarmRule> lists = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId, AlarmRuleEntity.READSET_FULL);

			for (AlarmRule rule : lists) {
				int ruleId = rule.getId();
				UserAlarmSubState userSubState = new UserAlarmSubState(rule);

				userRules.add(userSubState);
				try {
					m_alarmRuleSubscriptionDao.findByPK(ruleId, userId, AlarmRuleSubscriptionEntity.READSET_FULL);
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
		Collections.sort(userRules,new UserAlarmSubStateCompartor());
		model.setUserSubStates(userRules);
	}

	public void queryServiceRuleList(Model model,int userId) {
		List<UserAlarmSubState> userRules = new ArrayList<UserAlarmSubState>();
		try {
			int templateId = queryTemplateByName("service").getId();
			List<AlarmRule> lists = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId, AlarmRuleEntity.READSET_FULL);

			for (AlarmRule rule : lists) {
				int ruleId = rule.getId();
				UserAlarmSubState userSubState = new UserAlarmSubState(rule);

				userRules.add(userSubState);
				try {
					m_alarmRuleSubscriptionDao.findByPK(ruleId, userId, AlarmRuleSubscriptionEntity.READSET_FULL);
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
		Collections.sort(userRules,new UserAlarmSubStateCompartor());
		model.setUserSubStates(userRules);
	}

	public AlarmTemplate queryTemplateByName(String name) {
		try {
			AlarmTemplate entity = m_alarmTemplateDao.findAlarmTemplateByName(name, AlarmTemplateEntity.READSET_FULL);

			return entity;
		} catch (DalNotFoundException nfe) {
		} catch (DalException e) {
			Cat.logError(e);
		}

		throw new RuntimeException("Template Can't be null!");
	}

	public void ruleAdd(Payload payload, Model model) {
		List<String> domains = new ArrayList<String>();

		model.setDomains(domains);

	}

	public void ruleAddSubmit(Payload payload, Model model) {
		String domain = payload.getDomain();
		String content = payload.getContent();
		String type = payload.getType();
		int templateId = queryTemplateByName(type).getId();

		AlarmRule entity = m_alarmRuleDao.createLocal();
		entity.setContent(content);
		entity.setDomain(domain);
		entity.setTemplateId(templateId);

		try {
			m_alarmRuleDao.insert(entity);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}

	public void ruleDelete(Payload payload) {
		int id = payload.getAlarmRuleId();
		AlarmRule proto = m_alarmRuleDao.createLocal();

		proto.setKeyId(id);

		try {
			m_alarmRuleDao.deleteByPK(proto);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
	
	public void ruleSub(Payload payload,int loginId) {
		int subState = payload.getUserSubState();
		int alarmRuleId = payload.getAlarmRuleId();

		AlarmRuleSubscription alarmRuleSubscription = m_alarmRuleSubscriptionDao.createLocal();

		alarmRuleSubscription.setKeyAlarmRuleId(alarmRuleId);
		alarmRuleSubscription.setKeyUserId(loginId);
		alarmRuleSubscription.setUserId(loginId);
		alarmRuleSubscription.setAlarmRuleId(alarmRuleId);

		try {
			if (subState == 1) {
				m_alarmRuleSubscriptionDao.deleteByPK(alarmRuleSubscription);
			} else {
				m_alarmRuleSubscriptionDao.insert(alarmRuleSubscription);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void ruleUpdate(Payload payload, Model model) {
		int id = payload.getAlarmRuleId();

		try {
			AlarmRule alarmRule = m_alarmRuleDao.findByPK(id, AlarmRuleEntity.READSET_FULL);
			model.setAlarmRule(alarmRule);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
	public void ruleUpdateSubmit(Payload payload, Model model) {
		int id = payload.getAlarmRuleId();
		String content = payload.getContent();
		AlarmRule entity = m_alarmRuleDao.createLocal();

		entity.setContent(content);
		entity.setKeyId(id);
		try {
			m_alarmRuleDao.updateByPK(entity, AlarmRuleEntity.UPDATESET_UPDATE_CONTENT);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}
}
