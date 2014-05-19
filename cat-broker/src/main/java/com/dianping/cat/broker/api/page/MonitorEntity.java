package com.dianping.cat.broker.api.page;

public class MonitorEntity {

	private long m_timestamp;

	private String m_targetUrl;

	private double m_duration;

	private String m_httpStatus;

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

	public long getTimestamp() {
		return m_timestamp;
	}

	public MonitorEntity setTimestamp(long timestamp) {
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
	
	public String getHttpStatus() {
   	return m_httpStatus;
   }

	public MonitorEntity setHttpStatus(String httpStatus) {
   	m_httpStatus = httpStatus;
   	return this;
   }

	public String getErrorCode() {
		return m_errorCode;
	}

	public MonitorEntity setErrorCode(String errorCode) {
		m_errorCode = errorCode;
		return this;
	}

	@Override
   public String toString() {
	   return "MonitorEntity [m_timestamp=" + m_timestamp + ", m_targetUrl=" + m_targetUrl + ", m_duration="
	         + m_duration + ", m_httpStatus=" + m_httpStatus + ", m_errorCode=" + m_errorCode + ", m_city=" + m_city
	         + ", m_channel=" + m_channel + ", m_ip=" + m_ip + "]";
   }
	
}
