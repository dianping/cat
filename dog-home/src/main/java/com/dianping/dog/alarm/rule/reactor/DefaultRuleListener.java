package com.dianping.dog.alarm.rule.reactor;

import com.dianping.dog.alarm.rule.RuleContext;
import com.dianping.dog.alarm.rule.RuleEntity;
import com.dianping.dog.event.DataEvent;
import com.dianping.dog.event.Event;

public class DefaultRuleListener extends AbstractRuleReactor<DataEvent> {
	
	private RuleEntity m_ruleEntity;

	@Override
   public boolean isEligible(DataEvent event) {
	   return false;
   }

	@Override
   protected Event createNextEvent(RuleContext ctx) {
	   return null;
   }

	@Override
   protected void prepare(RuleContext ctx, DataEvent event) {
	   
   }

}
