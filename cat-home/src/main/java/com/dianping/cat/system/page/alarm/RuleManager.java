package com.dianping.cat.system.page.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.home.dal.user.DpAdminLogin;
import com.dainping.cat.home.dal.user.DpAdminLoginDao;
import com.dainping.cat.home.dal.user.DpAdminLoginEntity;
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

public class RuleManager {

	@Inject
	private AlarmRuleDao m_alarmRuleDao;

	@Inject
	private AlarmRuleSubscriptionDao m_alarmRuleSubscriptionDao;

	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;

	@Inject
	private DpAdminLoginDao m_dpAdminLoginDao;

	public void queryExceptionRuleList(Model model, int userId) {
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
		Collections.sort(userRules, new UserAlarmSubStateCompartor());
		model.setUserSubStates(userRules);
	}

	public void queryServiceRuleList(Model model, int userId) {
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
		Collections.sort(userRules, new UserAlarmSubStateCompartor());
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

	public List<String> queryUserMailsByRuleId(int alarmRuleId) {
		List<String> mails = new ArrayList<String>();
		try {
			List<AlarmRuleSubscription> alarmRuleSubscriptions = m_alarmRuleSubscriptionDao.findByAlarmRuleId(alarmRuleId,
			      AlarmRuleSubscriptionEntity.READSET_FULL);

			for (AlarmRuleSubscription alarmRule : alarmRuleSubscriptions) {
				int userId = alarmRule.getUserId();

				try {
					DpAdminLogin entity = m_dpAdminLoginDao.findByPK(userId, DpAdminLoginEntity.READSET_FULL);
					mails.add(entity.getEmail());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (DalNotFoundException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}

		return mails;
	}

	public List<String> queryUserPhonesByRuleId(int alarmRuleId) {
		List<String> phones = new ArrayList<String>();
		try {
			List<AlarmRuleSubscription> alarmRuleSubscriptions = m_alarmRuleSubscriptionDao.findByAlarmRuleId(alarmRuleId,
			      AlarmRuleSubscriptionEntity.READSET_FULL);

			for (AlarmRuleSubscription alarmRule : alarmRuleSubscriptions) {
				int userId = alarmRule.getUserId();

				try {
					DpAdminLogin entity = m_dpAdminLoginDao.findByPK(userId, DpAdminLoginEntity.READSET_FULL);
					phones.add(entity.getMobileNo());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (DalNotFoundException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}

		return phones;
	}

	public void ruleAdd(Payload payload, Model model) {
		List<String> domains = new ArrayList<String>();

		model.setDomains(domains);
		AlarmTemplate template = queryTemplateByName(payload.getType());
		model.setAlarmTemplate(template);
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

	public boolean ruleSub(Payload payload, int loginId) {
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
			return false;
		}
		return true;
	}

	public void ruleUpdate(Payload payload, Model model) {
		int id = payload.getAlarmRuleId();

		try {
			AlarmRule alarmRule = m_alarmRuleDao.findByPK(id, AlarmRuleEntity.READSET_FULL);
			model.setAlarmRule(alarmRule);

			int templatedId = alarmRule.getTemplateId();
			AlarmTemplate template = m_alarmTemplateDao.findByPK(templatedId, AlarmTemplateEntity.READSET_FULL);
			model.setAlarmTemplate(template);
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
