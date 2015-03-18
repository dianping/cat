package com.dianping.cat.broker.api.app.proto;

public class AppConnectionProto extends ProtoData {

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

	public AppConnectionProto() {
	}
	
	public AppConnectionProto addCount(int count) {
		m_count = m_count + count;
		return this;
	}

	public AppConnectionProto addRequestByte(int requestByte) {
		m_requestByte = m_requestByte + requestByte;
		return this;
	}

	public AppConnectionProto addResponseByte(int responseByte) {
		m_responseByte = m_responseByte + responseByte;
		return this;
	}

	public AppConnectionProto addResponseTime(int responseTime) {
		m_responseTime = m_responseTime + responseTime;
		return this;
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

	public AppConnectionProto setCity(int city) {
		m_city = city;
		return this;
	}

	public AppConnectionProto setCode(int code) {
		m_code = code;
		return this;
	}

	public AppConnectionProto setCommand(int command) {
		m_command = command;
		return this;
	}

	public AppConnectionProto setConnectType(int connectType) {
		m_connectType = connectType;
		return this;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public AppConnectionProto setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AppConnectionProto setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AppConnectionProto setPlatform(int platform) {
		m_platform = platform;
		return this;
	}

	public void setRequestByte(int requestByte) {
		m_requestByte = requestByte;
	}

	public void setResponseByte(int responseByte) {
		m_responseByte = responseByte;
	}

	public void setResponseTime(int responseTime) {
		m_responseTime = responseTime;
	}

	public AppConnectionProto setVersion(int version) {
		m_version = version;
		return this;
	}
}
