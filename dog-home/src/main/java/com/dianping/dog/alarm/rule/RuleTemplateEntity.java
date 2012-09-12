package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.entity.Duration;

public class RuleTemplateEntity {
	
	private int ruleTemplateID;
	
	private String templateName;
	
	private long interval;
	
	private List<Duration> durations;

	public int getRuleTemplateID() {
   	return ruleTemplateID;
   }

	public void setRuleTemplateID(int ruleTemplateID) {
   	this.ruleTemplateID = ruleTemplateID;
   }

	public String getTemplateName() {
   	return templateName;
   }

	public void setTemplateName(String templateName) {
   	this.templateName = templateName;
   }

	public long getInterval() {
   	return interval;
   }

	public void setInterval(long interval) {
   	this.interval = interval;
   }
	
	public void addDuration(Duration duration){
		durations.add(duration);
	}
	
	public void deleteDuration(Duration duration){
		durations.remove(duration);
	}

	public List<Duration> getDurations() {
   	return durations;
   }

	public void setDurations(List<Duration> durations) {
   	this.durations = durations;
   }
	
}
