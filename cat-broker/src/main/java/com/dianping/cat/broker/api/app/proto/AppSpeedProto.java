package com.dianping.cat.broker.api.app.proto;

public class AppSpeedProto extends ProtoData {

	private int m_speedId;

	private int m_city;

	private int m_operator;

	private int m_network;

	private int m_version;

	private int m_platform;

	private long m_count;

	private long m_slowCount;

	private long m_responseTime;

	private long m_slowResponseTime;

	public AppSpeedProto() {
	}

	public AppSpeedProto addCount(long count) {
		m_count += count;
		return this;
	}

	public AppSpeedProto addResponseTime(long responseTime) {
		m_responseTime += responseTime;
		return this;
	}

	public AppSpeedProto addSlowCount(long count) {
		m_slowCount += count;
		return this;
	}

	public AppSpeedProto addSlowResponseTime(long responseTime) {
		m_slowResponseTime += responseTime;
		return this;
	}

	public int getCity() {
		return m_city;
	}

	public long getCount() {
		return m_count;
	}

	public int getNetwork() {
		return m_network;
	}

	public int getOperator() {
		return m_operator;
	}

	public int getPlatform() {
		return m_platform;
	}

	public long getResponseTime() {
		return m_responseTime;
	}

	public long getSlowCount() {
		return m_slowCount;
	}

	public long getSlowResponseTime() {
		return m_slowResponseTime;
	}

	public int getSpeedId() {
		return m_speedId;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public int getVersion() {
		return m_version;
	}

	public AppSpeedProto setCity(int city) {
		m_city = city;
		return this;
	}

	public void setCount(long count) {
		m_count = count;
	}

	public AppSpeedProto setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AppSpeedProto setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AppSpeedProto setPlatform(int platform) {
		m_platform = platform;
		return this;
	}

	public void setResponseTime(long responseTime) {
		m_responseTime = responseTime;
	}

	public void setSlowCount(long slowCount) {
		m_slowCount = slowCount;
	}

	public void setSlowResponseTime(long slowResponseTime) {
		m_slowResponseTime = slowResponseTime;
	}

	public void setSpeedId(int speedId) {
		m_speedId = speedId;
	}

	public AppSpeedProto setVersion(int version) {
		m_version = version;
		return this;
	}
}
