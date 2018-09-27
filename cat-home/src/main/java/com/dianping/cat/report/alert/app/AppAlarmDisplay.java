package com.dianping.cat.report.alert.app;

import com.dianping.cat.app.AppDataField;

public class AppAlarmDisplay {

	private int m_command;

	private String m_commandName;

	private String m_code;

	private String m_network;

	private String m_version;

	private String m_connectType;

	private String m_platform;

	private String m_city;

	private String m_operator;

	private String m_metric;

	private AppDataField m_groupBy;

	public String getAlarmDesc() {
		return "告警维度 [命令字:" + m_commandName + ", 返回码=" + m_code + ", 网络类型=" + m_network + ", 版本=" + m_version + ", 链接类型="
		      + m_connectType + ", 平台=" + m_platform + ", 城市=" + m_city + ", 运营商=" + m_operator + ", 告警指标=" + m_metric
		      + "]";
	}

	public String getCity() {
		return m_city;
	}

	public String getCode() {
		return m_code;
	}

	public int getCommand() {
		return m_command;
	}

	public String getCommandName() {
		return m_commandName;
	}

	public String getConnectType() {
		return m_connectType;
	}

	public boolean getEachAlarm() {
		return m_groupBy != null;
	}

	public AppDataField getGroupBy() {
		return m_groupBy;
	}

	public String getMetric() {
		return m_metric;
	}

	public String getNetwork() {
		return m_network;
	}

	public String getOperator() {
		return m_operator;
	}

	public String getPlatform() {
		return m_platform;
	}

	public String getVersion() {
		return m_version;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public void setCode(String code) {
		m_code = code;
	}

	public void setCommand(int command) {
		m_command = command;
	}

	public void setCommandName(String commandName) {
		m_commandName = commandName;
	}

	public void setConnectType(String connectType) {
		m_connectType = connectType;
	}

	public void setGroupBy(AppDataField groupBy) {
		m_groupBy = groupBy;
	}

	public void setMetric(String metric) {
		m_metric = metric;
	}

	public void setNetwork(String network) {
		m_network = network;
	}

	public void setOperator(String operator) {
		m_operator = operator;
	}

	public void setPlatform(String platform) {
		m_platform = platform;
	}

	public void setVersion(String version) {
		m_version = version;
	}

}
