package com.dianping.dog.connector;

import com.dianping.dog.alarm.rule.RuleEntity;

public class ConnectorContext implements Comparable<ConnectorContext> {
	
	private ConnectorType type;
	
	private String url;
	
	private RuleEntity m_entity;
	
	ConnectorContext(RuleEntity entity){
		m_entity = entity;
	}
	
	public long getRuleId(){
		return 0;
	}
	
	public ConnectorType getType(){
		return type;
	}
	
	public String getUrl(){
		return url;
	}
	

	public RuleEntity getEntity() {
   	return m_entity;
   }

	@Override
	public int compareTo(ConnectorContext o) {
		return 0;
	}

}
