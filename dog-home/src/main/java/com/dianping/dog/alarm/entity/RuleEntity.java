package com.dianping.dog.alarm.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.dog.alarm.rule.RuleType;

/***
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 **/
public class RuleEntity {

	private int id;

	private String domain;

	private String ip;

	private String type;

	private String name;

	private String connectSource;// cat

	private String report;

	private String baseUrl;

	private RuleType ruleType;

	private List<Duration> durations;

	private ConnectEntity connect;

	private int period;

	private Date gmtModified;

	public String getDomain() {
		return domain;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getConnectSource() {
		return connectSource;
	}

	public void setConnectSource(String connectSource) {
		this.connectSource = connectSource;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public List<Duration> getDurations() {
		return durations;
	}

	public void addDuration(Duration duration) {
		if (durations == null) {
			durations = new ArrayList<Duration>();
		}
		duration.setGmtModified(this.gmtModified);
		durations.add(duration);
	}
	
	public ConnectEntity getConnect(){
		if(connect == null){
			ConnectEntity entity = new ConnectEntity();
			entity.setBaseUrl(this.baseUrl);
			entity.setConId(this.id);
			entity.setConnectSource(this.connectSource);
			entity.setDomain(this.domain);
			entity.setEntity(this);
			entity.setGmtModified(this.gmtModified);
			entity.setName(this.name);
			entity.setReport(this.report);
			entity.setType(this.type);
			this.connect = entity;
		}
		return connect;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	
	public String getUnicodeString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.ip + "@");
		sb.append(this.report +"@");
		sb.append(this.domain + "@");
		sb.append(this.type + "@");
		sb.append(this.name);
		return sb.toString();
	}

}
