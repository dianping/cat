package com.dianping.dog.alarm.rule;

public enum RuleType {
<<<<<<< HEAD
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
	
	
=======
    Exception,
    Service
>>>>>>> c6a52ce6cad28d0ed83daaaeff837d65f97edb26
}
