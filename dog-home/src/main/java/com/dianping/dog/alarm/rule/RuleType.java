package com.dianping.dog.alarm.rule;

public enum RuleType {
	Exception("exception"),

	Service("service");
	
	private String m_name;
	
	RuleType(String name){
		m_name=name;
	}

	public String getName() {
   	return m_name;
   }

	public void setName(String name) {
   	m_name = name;
   }
}
