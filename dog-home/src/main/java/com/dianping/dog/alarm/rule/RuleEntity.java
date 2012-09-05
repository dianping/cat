package com.dianping.dog.alarm.rule;

import java.util.List;

/* *
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 * */
public class RuleEntity {

	private long id;

	private String ruleType;

	private String domain;

	private String report;

	private String type;

	private String name;

	private List<Duration> durations;

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

	private List<AlarmStrategyEntity> strategys;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public List<AlarmStrategyEntity> getStrategys() {
		return strategys;
	}

	public void setStrategys(List<AlarmStrategyEntity> strategys) {
		this.strategys = strategys;
	}

}
