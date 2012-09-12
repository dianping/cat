package com.dianping.dog.alarm.rule;

import java.util.List;

/* *
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 * */
public class RuleEntity {
	
	private static String CONNECT_TYPE="http";

	private static String CONNECT_SOURCE="cat";
	
	private long id;

	private String domain;

	private String name;

	private String type;
	
	private String reportType;

	private List<Duration> durations;

	private ConnectEntity connect;

	private long interval;
	
	private RuleTemplateEntity ruleTemplateEntity;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getInterval() {
		if(interval==0){
			interval=ruleTemplateEntity.getInterval();
		}
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public String getRuleType() {
		return reportType;
	}

	public void setRuleType(String ruleType) {
		this.reportType = ruleType;
	}

	public List<Duration> getDurations() {
		if(durations==null||durations.size()==0){
			durations=ruleTemplateEntity.getDurations();
		}
		return durations;
	}

	public void setDurations(List<Duration> durations) {
		this.durations = durations;
	}

	public ConnectEntity getConnect() {
		ConnectEntity connect=new ConnectEntity();
		connect.setDomain(domain);
		connect.setName(name);
		connect.setType(type);
		connect.setReport(reportType);
		connect.setConnectSource(CONNECT_SOURCE);
		connect.setConnectType(CONNECT_TYPE);
		return connect;
	}

	public void setConnect(ConnectEntity connect) {
		this.connect = connect;
	}

	public RuleTemplateEntity getRuleTemplateEntity() {
   	return ruleTemplateEntity;
   }

	public void setRuleTemplateEntity(RuleTemplateEntity ruleTemplateEntity) {
   	this.ruleTemplateEntity = ruleTemplateEntity;
   }

}
