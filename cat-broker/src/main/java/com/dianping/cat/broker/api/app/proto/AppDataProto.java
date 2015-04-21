package com.dianping.cat.broker.api.app.proto;

public class AppDataProto extends ProtoData {

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

	private String m_ip;

	private String m_dpid;

	private String m_cityStr;

	private String m_operatorStr;
	
	private String m_commandStr;
	
	public String getCommandStr() {
		return m_commandStr;
	}

	public void setCommandStr(String commandStr) {
		m_commandStr = commandStr;
	}

	public String getCityStr() {
		return m_cityStr;
	}

	public void setCityStr(String cityStr) {
		m_cityStr = cityStr;
	}
	
	public String getOperatorStr() {
		return m_operatorStr;
	}

	public void setOperatorStr(String operatorStr) {
		m_operatorStr = operatorStr;
	}

	public AppDataProto addCount(int count) {
		m_count = m_count + count;
		return this;
	}

	public AppDataProto addRequestByte(int requestByte) {
		m_requestByte = m_requestByte + requestByte;
		return this;
	}

	public AppDataProto addResponseByte(int responseByte) {
		m_responseByte = m_responseByte + responseByte;
		return this;
	}

	public AppDataProto addResponseTime(int responseTime) {
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

	public String getDpid() {
		return m_dpid;
	}

	public String getIp() {
		return m_ip;
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

	public AppDataProto setCity(int city) {
		m_city = city;
		return this;
	}

	public AppDataProto setCode(int code) {
		m_code = code;
		return this;
	}

	public AppDataProto setCommand(int command) {
		m_command = command;
		return this;
	}

	public AppDataProto setConnectType(int connectType) {
		m_connectType = connectType;
		return this;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public AppDataProto setDpid(String dpid) {
		m_dpid = dpid;
		return this;
	}

	public AppDataProto setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public AppDataProto setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AppDataProto setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AppDataProto setPlatform(int platform) {
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

	public AppDataProto setVersion(int version) {
		m_version = version;
		return this;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		return sb.toString();
	}

}
