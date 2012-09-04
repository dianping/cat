package com.dianping.dog.alarm.rule;

public enum AlarmStrategyEntity {
	
   SMS("SMS Strategy"),
   EMAIL("Email Strategy");
   
	private long m_interval;
	
	private String m_name;
   
   AlarmStrategyEntity(String name){
   	this.m_name = name;
   }
   
	public long getInterval() {
   	return m_interval;
   }

	public void setInterval(long interval) {
   	this.m_interval = interval;
   }

	public String getName() {
   	return m_name;
   }
	
}
