package com.dianping.dog.alarm.entity;

import java.util.Date;
import java.util.List;

import com.dianping.dog.alarm.rule.RuleType;

/***
 *  @author yanchun.yang 
 *  从数据库中读取的规则配置实体类
 **/
public class RuleEntity {

	private long id;
	
	private String name;
	
	private RuleType ruleType;

	private List<Duration> durations;

	private ConnectEntity connect;
	
	private Date gmtModified;
	
	private long interval;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
   	return name;
   }

	public void setName(String name) {
   	this.name = name;
   }

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public List<Duration> getDurations() {
		return durations;
	}

	public void setDurations(List<Duration> durations) {
		this.durations = durations;
	}

	public ConnectEntity getConnect() {
		return connect;
	}

	public void setConnect(ConnectEntity connect) {
		this.connect = connect;
	}

	public Date getGmtModified() {
   	return gmtModified;
   }

	public void setGmtModified(Date gmtModified) {
   	this.gmtModified = gmtModified;
   }
	
}
