package com.dianping.cat.system.alarm.threshold;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.AlarmRule;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleEntity;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateEntity;
import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.threshold.template.ThresholdTemplateMerger;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class ThresholdRuleManager implements Initializable {

	@Inject
	private AlarmRuleDao m_alarmRuleDao;

	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;

	public Map<Integer, Date> m_exceptionModifyTimes = new HashMap<Integer, Date>();

	public Map<Integer, Date> m_serviceModifyTimes = new HashMap<Integer, Date>();

	public final Map<String, ArrayList<ThresholdRule>> m_exceptionRules = new HashMap<String, ArrayList<ThresholdRule>>();

	public final Map<String, ArrayList<ThresholdRule>> m_serviceRules = new HashMap<String, ArrayList<ThresholdRule>>();

	private void addExceptionRule(AlarmRule rule, ThresholdTemplate template) {
		String domain = rule.getDomain();
		ThresholdRule thresholdRule = new ThresholdRule(rule.getId(), domain, template);

		ArrayList<ThresholdRule> rules = m_exceptionRules.get(domain);

		if (rules == null) {
			rules = new ArrayList<ThresholdRule>();

			rules.add(thresholdRule);
			m_exceptionRules.put(domain, rules);
		} else {
			rules.add(thresholdRule);
		}
	}

	private void addServiceRule(AlarmRule rule, ThresholdTemplate template) {
		String domain = rule.getDomain();
		ThresholdRule thresholdRule = new ThresholdRule(rule.getId(), domain, template);

		ArrayList<ThresholdRule> rules = m_serviceRules.get(domain);

		if (rules == null) {
			rules = new ArrayList<ThresholdRule>();

			rules.add(thresholdRule);
			m_serviceRules.put(domain, rules);
		} else {
			rules.add(thresholdRule);
		}
	}

	public List<ThresholdRule> getAllExceptionRules() {
		List<ThresholdRule> result = new ArrayList<ThresholdRule>();

		for (ArrayList<ThresholdRule> rule : m_exceptionRules.values()) {
			result.addAll(rule);
		}
		return result;
	}

	public List<ThresholdRule> getAllServiceRules() {
		List<ThresholdRule> result = new ArrayList<ThresholdRule>();

		for (ArrayList<ThresholdRule> rule : m_serviceRules.values()) {
			result.addAll(rule);
		}
		return result;
	}

	public List<ThresholdRule> getExceptionRuleByDomain(String domain) {
		ArrayList<ThresholdRule> arrayList = m_exceptionRules.get(domain);

		if (arrayList != null) {
			return arrayList;
		}
		return new ArrayList<ThresholdRule>();
	}

	public List<ThresholdRule> getServiceRuleByDomain(String domain) {
		ArrayList<ThresholdRule> arrayList = m_serviceRules.get(domain);

		if (arrayList != null) {
			return arrayList;
		}
		return new ArrayList<ThresholdRule>();
	}

	@Override
	public void initialize() throws InitializationException {
		initalizeExceptionRule();
		initalizeServiceRule();

		ReloadThresholdRuleTask task = new ReloadThresholdRuleTask();

		Threads.forGroup("Cat-Alarm").start(task);
	}

	private void initalizeExceptionRule() {
		try {
			AlarmTemplate alarmTemplate = m_alarmTemplateDao.findAlarmTemplateByName("exception",
			      AlarmTemplateEntity.READSET_FULL);
			int templateId = alarmTemplate.getId();
			String content = alarmTemplate.getContent();
			ThresholdTemplate baseTemplate = DefaultSaxParser.parse(content);

			List<AlarmRule> exceptionRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId,
			      AlarmRuleEntity.READSET_FULL);

			for (AlarmRule rule : exceptionRules) {
				m_exceptionModifyTimes.put(rule.getId(), rule.getModifyDate());

				try {
					String newContent = rule.getContent().trim();
					ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

					addExceptionRule(rule, template);
					m_exceptionModifyTimes.put(rule.getId(), rule.getModifyDate());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initalizeServiceRule() {
		try {
			AlarmTemplate alarmTemplate = m_alarmTemplateDao.findAlarmTemplateByName("service",
			      AlarmTemplateEntity.READSET_FULL);
			int templateId = alarmTemplate.getId();
			String content = alarmTemplate.getContent();
			ThresholdTemplate baseTemplate = DefaultSaxParser.parse(content);

			List<AlarmRule> serviceRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId,
			      AlarmRuleEntity.READSET_FULL);

			for (AlarmRule rule : serviceRules) {
				m_serviceModifyTimes.put(rule.getId(), rule.getModifyDate());

				try {
					String newContent = rule.getContent().trim();
					ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

					addServiceRule(rule, template);
					m_serviceModifyTimes.put(rule.getId(), rule.getModifyDate());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ThresholdTemplate mergerTemplate(ThresholdTemplate baseTemplate, String newContent) {
		if (newContent == null || newContent.length() > 0) {
			try {
				ThresholdTemplate newTemplate = DefaultSaxParser.parse(newContent);
				ThresholdTemplateMerger merger = new ThresholdTemplateMerger(new ThresholdTemplate());

				baseTemplate.accept(merger);
				newTemplate.accept(merger);

				ThresholdTemplate template = merger.getThresholdTemplate();
				return template;
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return baseTemplate;
	}

	private void refreshExceptionRules() {
		synchronized (m_exceptionRules) {
			try {
				AlarmTemplate alarmTemplate = m_alarmTemplateDao.findAlarmTemplateByName("exception",
				      AlarmTemplateEntity.READSET_FULL);
				int templateId = alarmTemplate.getId();
				String content = alarmTemplate.getContent();
				ThresholdTemplate baseTemplate = DefaultSaxParser.parse(content);

				List<AlarmRule> exceptionRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId,
				      AlarmRuleEntity.READSET_FULL);

				Set<Integer> allIds = new HashSet<Integer>();
				for (AlarmRule alarmRule : exceptionRules) {
					int id = alarmRule.getId();
					allIds.add(id);
					Date date = m_exceptionModifyTimes.get(id);
					if (date == null) {
						String newContent = alarmRule.getContent();
						ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

						addExceptionRule(alarmRule, template);
						Cat.getProducer().logEvent("ExceptionRule", "Add", Event.SUCCESS, null);
					} else {
						Date modifyDate = alarmRule.getModifyDate();

						if (date.getTime() < modifyDate.getTime()) {
							Cat.getProducer().logEvent("ExceptionRule", "Update", Event.SUCCESS, null);
							String newContent = alarmRule.getContent();
							ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

							String domain = alarmRule.getDomain();
							ArrayList<ThresholdRule> ruleList = m_exceptionRules.get(domain);
							for (ThresholdRule rule : ruleList) {
								if (rule.getRuleId() == alarmRule.getId()) {
									rule.refreshTemplate(template);
									m_exceptionModifyTimes.put(rule.getRuleId(), modifyDate);
									break;

								}
							}
						}
					}
				}

				for (ArrayList<ThresholdRule> rules : m_exceptionRules.values()) {
					List<ThresholdRule> removes = new ArrayList<ThresholdRule>();

					for (ThresholdRule rule : rules) {
						int id = rule.getRuleId();

						if (!allIds.contains(id)) {
							removes.add(rule);
						}
					}
					for (ThresholdRule rule : removes) {
						rules.remove(rule);
						m_exceptionModifyTimes.remove(rule.getRuleId());
						Cat.getProducer().logEvent("ExceptionRule", "Delete", Event.SUCCESS, null);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private void refreshServiceRules() {
		synchronized (m_serviceRules) {
			try {
				AlarmTemplate alarmTemplate = m_alarmTemplateDao.findAlarmTemplateByName("service",
				      AlarmTemplateEntity.READSET_FULL);
				int templateId = alarmTemplate.getId();
				String content = alarmTemplate.getContent();
				ThresholdTemplate baseTemplate = DefaultSaxParser.parse(content);

				List<AlarmRule> serviceRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId,
				      AlarmRuleEntity.READSET_FULL);

				Set<Integer> allIds = new HashSet<Integer>();
				for (AlarmRule alarmRule : serviceRules) {
					int id = alarmRule.getId();
					allIds.add(id);
					Date date = m_serviceModifyTimes.get(id);
					if (date == null) {
						String newContent = alarmRule.getContent();
						ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

						addServiceRule(alarmRule, template);
						Cat.getProducer().logEvent("ServiceRule", "Add", Event.SUCCESS, null);
					} else {
						Date modifyDate = alarmRule.getModifyDate();

						if (date.getTime() < modifyDate.getTime()) {
							Cat.getProducer().logEvent("ServiceRule", "Update", Event.SUCCESS, null);
							String newContent = alarmRule.getContent();
							ThresholdTemplate template = mergerTemplate(baseTemplate, newContent);

							String domain = alarmRule.getDomain();
							ArrayList<ThresholdRule> ruleList = m_serviceRules.get(domain);
							for (ThresholdRule rule : ruleList) {
								if (rule.getRuleId() == alarmRule.getId()) {
									rule.refreshTemplate(template);
									m_serviceModifyTimes.put(rule.getRuleId(), modifyDate);
									break;

								}
							}
						}
					}
				}

				for (ArrayList<ThresholdRule> rules : m_serviceRules.values()) {
					List<ThresholdRule> removes = new ArrayList<ThresholdRule>();

					for (ThresholdRule rule : rules) {
						int id = rule.getRuleId();

						if (!allIds.contains(id)) {
							removes.add(rule);
						}
					}
					for (ThresholdRule rule : removes) {
						rules.remove(rule);
						m_serviceModifyTimes.remove(rule.getRuleId());
						Cat.getProducer().logEvent("ServiceRule", "Delete", Event.SUCCESS, null);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public class ReloadThresholdRuleTask implements Task {

		@Override
		public String getName() {
			return "exception-reload-rule";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				Transaction t = Cat.newTransaction("System", "RefreshRule");

				t.setStatus(Transaction.SUCCESS);
				try {
					refreshExceptionRules();
					refreshServiceRules();
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				} finally {
					t.complete();
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

}
