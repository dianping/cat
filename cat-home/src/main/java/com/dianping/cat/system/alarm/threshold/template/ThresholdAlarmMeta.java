package com.dianping.cat.system.alarm.threshold.template;

import java.util.Date;

import com.dianping.cat.home.template.entity.Duration;

public class ThresholdAlarmMeta {
	private String m_baseUrl;

	private Date m_date;

	private String m_domain;

	private Duration m_duration;

	private long m_realCount;

	private int m_ruleId;

	private String m_type;

	public String getBaseShowUrl() {
		// convert on line system ip to domain name
		String showUrl = m_baseUrl.replace("http://10.1.6.128:8080/", "http://cat.dianpingoa.com/");
		return showUrl;
	}

	public String getBaseUrl() {
		return m_baseUrl;
	}

	public Date getDate() {
		return m_date;
	}

	public String getDomain() {
		return m_domain;
	}

	public Duration getDuration() {
		return m_duration;
	}

	public long getRealCount() {
		return m_realCount;
	}

	public int getRuleId() {
		return m_ruleId;
	}

	public String getType() {
		return m_type;
	}

	public void setBaseUrl(String baseUrl) {
		m_baseUrl = baseUrl;
	}

	public ThresholdAlarmMeta setDate(Date date) {
		m_date = date;
		return this;
	}

	public ThresholdAlarmMeta setDomain(String domain) {
		m_domain = domain;
		return this;
	}

	public ThresholdAlarmMeta setDuration(Duration duration) {
		m_duration = duration;
		return this;
	}

	public ThresholdAlarmMeta setRealCount(long realCount) {
		m_realCount = realCount;
		return this;
	}

	public ThresholdAlarmMeta setRuleId(int ruleId) {
		m_ruleId = ruleId;
		return this;
	}

	public ThresholdAlarmMeta setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public String toString() {
		return "ThresholdAlarmMeta [baseUrl=" + m_baseUrl + ", date=" + m_date + ", domain=" + m_domain
		      + ", duration=" + m_duration + ", realCount=" + m_realCount + ", ruleId=" + m_ruleId + ", type="
		      + m_type + "]";
	}

}
