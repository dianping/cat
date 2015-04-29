package com.dianping.cat.mvc;

import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class ApiPayload {

	@FieldMeta("ip")
	private String m_ipAddress;

	@FieldMeta("messageId")
	private String m_messageId;

	@FieldMeta("waterfall")
	private boolean m_waterfall;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("city")
	private String m_city;

	@FieldMeta("channel")
	private String m_channel;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("metricType")
	private String m_metricType;

	@FieldMeta("database")
	private String m_database;

	@FieldMeta("province")
	private String m_province;

	@FieldMeta("queryType")
	private String m_queryType;

	@FieldMeta("min")
	private int m_min = -1;

	@FieldMeta("max")
	private int m_max = -1;

	@FieldMeta("cdn")
	private String m_cdn = "ALL";

	public String getCdn() {
		return m_cdn;
	}

	public String getChannel() {
		return m_channel;
	}

	public String getCity() {
		return m_city;
	}

	public String getDatabase() {
		return m_database;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public int getMax() {
		return m_max;
	}

	public String getMessageId() {
		return m_messageId;
	}

	public String getMetricType() {
		return m_metricType;
	}

	public int getMin() {
		return m_min;
	}

	public String getName() {
		return m_name;
	}

	public String getProvince() {
		return m_province;
	}

	public String getQueryType() {
		return m_queryType;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public String getType() {
		return m_type;
	}

	public boolean isWaterfall() {
		return m_waterfall;
	}

	public void setCdn(String cdn) {
		m_cdn = cdn;
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setMax(int max) {
		m_max = max;
	}

	public void setMessageId(String messageId) {
		m_messageId = messageId;
	}

	public void setMeticType(String metricType) {
		m_metricType = metricType;
	}

	public void setMin(int min) {
		m_min = min;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setProvince(String province) {
		m_province = province;
	}

	public void setQueryType(String queryType) {
		m_queryType = queryType;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setWaterfall(boolean waterfall) {
		m_waterfall = waterfall;
	}

}
