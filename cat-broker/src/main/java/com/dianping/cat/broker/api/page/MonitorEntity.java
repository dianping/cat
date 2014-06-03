package com.dianping.cat.broker.api.page;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MonitorEntity {

	private long m_timestamp;

	private String m_targetUrl;

	private double m_duration;

	private String m_httpStatus;

	private String m_errorCode;

	private String m_ip;

	private int m_count = 1;

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public double getDuration() {
		return m_duration;
	}

	public String getErrorCode() {
		return m_errorCode;
	}

	public String getHttpStatus() {
		return m_httpStatus;
	}

	public String getIp() {
		return m_ip;
	}

	public String getTargetUrl() {
		return m_targetUrl;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public MonitorEntity setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	public MonitorEntity setErrorCode(String errorCode) {
		m_errorCode = errorCode;
		return this;
	}

	public MonitorEntity setHttpStatus(String httpStatus) {
		m_httpStatus = httpStatus;
		return this;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public MonitorEntity setTargetUrl(String targetUrl) {
		try {
			m_targetUrl = URLDecoder.decode(targetUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			m_targetUrl = targetUrl;
		}
		return this;
	}

	public MonitorEntity setTimestamp(long timestamp) {
		m_timestamp = timestamp;
		return this;
	}

	@Override
	public String toString() {
		return "MonitorEntity [m_timestamp=" + m_timestamp + ", m_targetUrl=" + m_targetUrl + ", m_duration="
		      + m_duration + ", m_httpStatus=" + m_httpStatus + ", m_errorCode=" + m_errorCode + ", m_ip=" + m_ip + "]";
	}

}
