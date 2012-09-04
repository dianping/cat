package com.dianping.dog.event;

import com.dianping.dog.entity.RuleEntity;

public class RuleEvent implements Event {

	private EventType m_type;
	
	private RuleEntity m_ruleEntity;

	public RuleEvent() {
		m_type = DefaultEventType.RULE_EVENT;
	}

	@Override
	public EventType getType() {
		return m_type;
	}
	
	public void setRuleEntity(RuleEntity entity){
		m_ruleEntity = entity;
	}

	public RuleEntity getRuleEntity(){
		return m_ruleEntity;
	}
}
