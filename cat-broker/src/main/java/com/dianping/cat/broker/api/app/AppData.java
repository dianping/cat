package com.dianping.cat.broker.api.app;

public class AppData {
	private long m_timestamp;

	private int m_city;

	private int m_operator;

	private int m_network;

	private int m_version;

	private int m_connectType;

	private int m_command;

	private int m_code;

	private int m_platform;

	private int m_count;

	private int m_requestByte;

	private int m_responseByte;

	private int m_responseTime;

	public AppData() {
	}

	public int getCity() {
		return m_city;
	}

	public int getCode() {
		return m_code;
	}

	public int getCommand() {
		return m_command;
	}

	public int getConnectType() {
		return m_connectType;
	}

	public int getCount() {
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

	public int getRequestByte() {
		return m_requestByte;
	}

	public int getResponseByte() {
		return m_responseByte;
	}

	public int getResponseTime() {
		return m_responseTime;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public int getVersion() {
		return m_version;
	}

	public AppData setCity(int city) {
		m_city = city;
		return this;
	}

	public AppData setCode(int code) {
		m_code = code;
		return this;
	}

	public AppData setCommand(int command) {
		m_command = command;
		return this;
	}

	public AppData setConnectType(int connectType) {
		m_connectType = connectType;
		return this;
	}

	public AppData setCount(int count) {
		m_count = count;
		return this;
	}

	public AppData setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AppData setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AppData setPlatform(int platform) {
		m_platform = platform;
		return this;
	}

	public AppData setRequestByte(int requestByte) {
		m_requestByte = requestByte;
		return this;
	}

	public AppData setResponseByte(int responseByte) {
		m_responseByte = responseByte;
		return this;
	}

	public AppData setResponseTime(int responseTime) {
		m_responseTime = responseTime;
		return this;
	}

	public AppData setTimestamp(long timestamp) {
		m_timestamp = timestamp;
		return this;
	}

	public AppData setVersion(int version) {
		m_version = version;
		return this;
	}

}
