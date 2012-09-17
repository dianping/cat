package com.dianping.dog.alarm.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.rule.exception.ExceptionRule;
import com.dianping.dog.event.EventDispatcher;
import com.site.lookup.annotation.Inject;

public class DefaultRuleManager implements RuleManager {
	
	@Inject
	protected EventDispatcher m_eventDispatcher;
	
	Map<Long,Rule> ruleMap = new ConcurrentHashMap<Long,Rule>();

	@Override
	public List<Rule> getRules() {
		List<Rule> ruleList =new ArrayList<Rule>();
		synchronized(ruleMap){
			for(Rule rule:ruleMap.values()){
				ruleList.add(rule);
			}
		}
		return ruleList;
	}

	@Override
	public Rule getRuleById(long id) {
	   return ruleMap.get(id);
	}

	@Override
   public boolean addRule(RuleEntity entity) {
	   if(entity.getRuleType() == RuleType.Exception){
	   	Rule rule = new ExceptionRule();
	   	rule.init(entity);
	   	rule.setDispatcher(m_eventDispatcher);
	   	ruleMap.put(rule.getRuleId(), rule);
	   } 
	   return true;
   }

}
