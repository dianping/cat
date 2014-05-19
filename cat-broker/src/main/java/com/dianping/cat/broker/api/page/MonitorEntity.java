package com.dianping.cat.broker.api.page;

public class MonitorEntity {

	private int m_timestamp;

	private String m_targetUrl;

	private double m_duration;

	private String m_httpCode;

	private String m_errorCode;

	private String m_city;

	private String m_channel;

	private String m_ip;

	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public String getChannel() {
		return m_channel;
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public int getTimestamp() {
		return m_timestamp;
	}

	public MonitorEntity setTimestamp(int timestamp) {
		m_timestamp = timestamp;
		return this;
	}

	public String getTargetUrl() {
		return m_targetUrl;
	}

	public MonitorEntity setTargetUrl(String targetUrl) {
		m_targetUrl = targetUrl;
		return this;
	}

	public double getDuration() {
		return m_duration;
	}

	public MonitorEntity setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	public String getHttpCode() {
		return m_httpCode;
	}

	public MonitorEntity setHttpCode(String httpCode) {
		m_httpCode = httpCode;
		return this;
	}

	public String getErrorCode() {
		return m_errorCode;
	}

	public MonitorEntity setErrorCode(String errorCode) {
		m_errorCode = errorCode;
		return this;
	}

}
