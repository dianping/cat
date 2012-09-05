package com.dianping.dog.alarm.rule;

import java.util.List;

/* *
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 * */
public class RuleEntity {

	private long id;

	private String ruleType;

	private List<Duration> durations;

	private ConnectEntity connect;

	static class Connect {
		private String connectType;// http

		private String connectSource;// cat

		private String domain;

		private String report;

		private String type;

		private String name;
	}

	static class Duration {
		private int min;

		private int max;

		private List<AlarmStrategyEntity> strategys;
	}

	private long interval;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
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

}
