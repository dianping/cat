package com.dianping.cat.alarm.app;

import com.dianping.cat.app.AppDataField;

public class AppAlarmRuleParam implements Cloneable {

	private int m_command;

	private String m_commandName;

	private int m_code;

	private int m_network;

	private int m_version;

	private int m_connectType;

	private int m_platform;

	private int m_city;

	private int m_operator;

	private String m_metric;

	private AppDataField m_groupBy;

	public int getCity() {
		return m_city;
	}

	public int getCode() {
		return m_code;
	}

	public int getCommand() {
		return m_command;
	}

	public String getCommandName() {
		return m_commandName;
	}

	public int getConnectType() {
		return m_connectType;
	}

	public AppDataField getGroupBy() {
		return m_groupBy;
	}

	public String getMetric() {
		return m_metric;
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

	public int getVersion() {
		return m_version;
	}

	public boolean isEachAlarm() {
		return m_groupBy != null;
	}

	public boolean getEachAlarm() {
		return m_groupBy != null;
	}

	public void setCity(int city) {
		m_city = city;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public void setCommand(int command) {
		m_command = command;
	}

	public void setCommandName(String commandName) {
		m_commandName = commandName;
	}

	public void setConnectType(int connectType) {
		m_connectType = connectType;
	}

	public void setGroupBy(AppDataField groupBy) {
		m_groupBy = groupBy;
	}

	public void setMetric(String metric) {
		m_metric = metric;
	}

	public void setNetwork(int network) {
		m_network = network;
	}

	public void setOperator(int operator) {
		m_operator = operator;
	}

	public void setPlatform(int platform) {
		m_platform = platform;
	}

	public void setVersion(int version) {
		m_version = version;
	}

	@Override
	public AppAlarmRuleParam clone() throws CloneNotSupportedException {
		AppAlarmRuleParam param = new AppAlarmRuleParam();

		param.setCommand(m_command);
		param.setCommandName(m_commandName);
		param.setCode(m_code);
		param.setConnectType(m_connectType);
		param.setMetric(m_metric);
		param.setGroupBy(m_groupBy);
		param.setNetwork(m_network);
		param.setCity(m_city);
		param.setOperator(m_operator);
		param.setPlatform(m_platform);
		param.setVersion(m_version);
		return param;
	}

	@Override
	public String toString() {
		return "AppAlarmRuleParam [m_command=" + m_command + ", m_commandName=" + m_commandName + ", m_code=" + m_code
		      + ", m_network=" + m_network + ", m_version=" + m_version + ", m_connectType=" + m_connectType
		      + ", m_platform=" + m_platform + ", m_city=" + m_city + ", m_operator=" + m_operator + ", m_metric="
		      + m_metric + ", m_groupBy=" + m_groupBy + "]";
	}

}
