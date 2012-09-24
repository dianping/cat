package com.dianping.dog.alarm.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.rule.exception.ExceptionRule;
import com.dianping.dog.dal.Ruleinstance;
import com.dianping.dog.dal.RuleinstanceDao;
import com.dianping.dog.dal.RuleinstanceEntity;
import com.dianping.dog.dal.Ruletemplate;
import com.dianping.dog.dal.RuletemplateDao;
import com.dianping.dog.dal.RuletemplateEntity;
import com.dianping.dog.event.EventDispatcher;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class DefaultRuleManager implements Initializable, RuleManager, LogEnabled {

	@Inject
	protected EventDispatcher m_eventDispatcher;

	@Inject
	protected RuleinstanceDao m_ruleinstanceDao;

	@Inject
	protected RuletemplateDao m_ruletemplateDao;

	@Inject
	protected RuleLoaderFactory m_ruleLoaderFactory;

	private Logger m_logger;

	private static final long SLEEP_TIME = 10 * 1000;// sleep for 10 seconds

	private Map<Integer, Rule> ruleMap = new HashMap<Integer, Rule>();

	private ReentrantLock lock = new ReentrantLock();

	@Override
	public List<Rule> getRules() {
		List<Rule> ruleList = new ArrayList<Rule>();
		lock.lock();
		try {
			for (Rule rule : ruleMap.values()) {
				ruleList.add(rule);
			}
		} finally {
			lock.unlock();
		}
		return ruleList;
	}

	@Override
	public Rule getRuleById(int id) {
		lock.lock();
		try {
			return ruleMap.get(id);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean addRule(RuleEntity entity) {
		lock.lock();
		try {
			return internalAddRule(entity);
		} finally {
			lock.unlock();
		}
	}

	private boolean internalAddRule(RuleEntity entity) {
		try {
			if (entity.getRuleType() == RuleType.Exception) {
				Rule rule = new ExceptionRule();
				rule.init(entity);
				rule.enableLogging(m_logger);
				rule.setDispatcher(m_eventDispatcher);
				ruleMap.put(rule.getRuleId(), rule);
				m_logger.info(String.format("Update or Insert Rule id:[%s]",rule.getRuleId()));
			}
		} catch (Exception e) {
			m_logger.error(String.format("fail to update rule,rule_id:%s ,[error]: %s", entity.getId(), e.getMessage()));
			return false;
		}
		return true;
	}

	private void refreshRules() throws Exception {
		Map<Integer, Ruletemplate> templateCash = new HashMap<Integer, Ruletemplate>();
		List<Ruleinstance> instances = m_ruleinstanceDao.findAllInstance(RuleinstanceEntity.READSET_FULL);
		Set<Integer> updatedRuleIds = new HashSet<Integer>();
		for (Ruleinstance instance : instances) {
			try {
				int templateId = instance.getTemplateId();
				Ruletemplate template = templateCash.get(templateId);
				if (template == null) {
					template = m_ruletemplateDao.findByPK(templateId, RuletemplateEntity.READSET_FULL);
					if (template == null) {
						m_logger.error(String.format("fail to get ruletemplate: [%s]", templateId));
						continue;
					}
					templateCash.put(templateId, template);
				}
				RuleLoader loader = m_ruleLoaderFactory.getRuleLoader(template);
				if (loader == null) {
					m_logger.error(String.format("fail to get RuleEntity for Template: [%s]", template.getName()));
					updatedRuleIds.add(instance.getId());// TODO 防止解析错误后，规则被删除。此处需要修改
					continue;
				}
				RuleEntity entity = loader.loadRuleEntity(instance, template);
				if (entity != null) {
					internalUpdate(entity);
				}
				updatedRuleIds.add(instance.getId());
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		removeExpiredRules(updatedRuleIds);
	}

	private void removeExpiredRules(Set<Integer> updatedRuleIds) {
		lock.lock();
		try {
			Iterator<Integer> ruleIds = this.ruleMap.keySet().iterator();
			while (ruleIds.hasNext()) {
				Integer ruleId = ruleIds.next();
				if (!updatedRuleIds.contains(ruleId)) {
					ruleIds.remove();
					m_logger.info(String.format("Remove Rule id:[%s]",ruleId));
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private void internalUpdate(RuleEntity entity) {
		lock.lock();
		try {
			Rule rule = this.getRuleById(entity.getId());
			if (rule == null) {
				internalAddRule(entity);
				return;
			}
			RuleEntity orginalEntity = rule.getRuleEntity();
			if (entity.getGmtModified().getTime() != orginalEntity.getGmtModified().getTime()) {
				this.ruleMap.remove(rule.getRuleId());
				internalAddRule(entity);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Dog").start(new ReloadRule());
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

	private class ReloadRule implements Task {

		@Override
		public void run() {
			while (true) {
				try {
					refreshRules();
				} catch (Exception e) {
					m_logger.error(e.getMessage());
					Cat.logError(e);
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					m_logger.error(String.format("fail to sleep for [Time]:%s ,[error]: %s", SLEEP_TIME, e.getMessage()));
				}
			}
		}

		@Override
		public String getName() {
			return "DefaultRuleReloader";
		}

		@Override
		public void shutdown() {

		}

	}
}
