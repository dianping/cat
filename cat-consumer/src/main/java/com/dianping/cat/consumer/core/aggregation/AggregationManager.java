package com.dianping.cat.consumer.core.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.AggregationRule;
import com.dainping.cat.consumer.core.dal.AggregationRuleDao;
import com.dainping.cat.consumer.core.dal.AggregationRuleEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;

public class AggregationManager implements Initializable {

	@Inject
	private AggregationRuleDao m_ruleDao;

	@Inject
	private ServerConfigManager m_serverConfigManager;
	
	@Inject
	protected AggregationHandler m_handler;

	private static Map<Integer, Map<String, List<AggregationRule>>> m_ruleMap = new HashMap<Integer, Map<String, List<AggregationRule>>>();

	public static final int ONE_MINUTE = 60 * 1000;

	public static final int TRANSACTION_TYPE = 1;

	public static final int EVENT_TYPE = 2;

	public static final int PROBLEM_TYPE = 3;
	
	@Override
	public void initialize() throws InitializationException {
		if (!m_serverConfigManager.isLocalMode()) {
			try {
				RuleReload reload = new RuleReload();
				Threads.forGroup("Cat").start(reload);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	/**
	 * Reload aggregation rule from database
	 */
	public void reload() {
		synchronized (m_ruleMap) {
			try {
				List<AggregationRule> ruleList = m_ruleDao.findAll(AggregationRuleEntity.READSET_FULL);
				if (ruleList.size() > 0) {
					Map<Integer, Map<String, List<AggregationRule>>> tmpRuleMap = new HashMap<Integer, Map<String, List<AggregationRule>>>();
					for (AggregationRule rule : ruleList) {
						int type = rule.getType();
						String domain = rule.getDomain();
						Map<String, List<AggregationRule>> typeRuleMap = null;
						List<AggregationRule> domainRules = null;
						if (tmpRuleMap.containsKey(type)) {
							typeRuleMap = tmpRuleMap.get(type);
							if (typeRuleMap.containsKey(domain)) {
								typeRuleMap.get(domain).add(rule);
							} else {
								domainRules = new ArrayList<AggregationRule>();
								domainRules.add(rule);
								typeRuleMap.put(domain, domainRules);
							}
						} else {
							domainRules = new ArrayList<AggregationRule>();
							domainRules.add(rule);
							typeRuleMap = new HashMap<String, List<AggregationRule>>();
							typeRuleMap.put(domain, domainRules);
							tmpRuleMap.put(type, typeRuleMap);
						}
					}
					m_ruleMap = tmpRuleMap;
					m_handler.register(m_ruleMap);
				}
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	/**
	 * Thread to reload aggregation rule from database
	 * @author renyuan.sun
	 *
	 */
	public class RuleReload implements Task {

		@Override
		public String getName() {
			return "Aggregation-Rule-Info-Reload";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					reload();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(3 * ONE_MINUTE);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	public String handle(int type, String domain, String status) {
	   return m_handler.handle(type, domain, status);
   }

	
}
