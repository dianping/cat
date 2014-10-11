package com.dianping.cat.config.app;

public class AppDataSpreadInfo {

	private int m_operator = QueryEntity.DEFAULT_VALUE;

	private int m_network = QueryEntity.DEFAULT_VALUE;

	private int m_appVersion = QueryEntity.DEFAULT_VALUE;

	private int m_connectType = QueryEntity.DEFAULT_VALUE;

	private int m_platform = QueryEntity.DEFAULT_VALUE;

	private int m_city = QueryEntity.DEFAULT_VALUE;

	private double m_successRatio;

	private long m_accessNumberSum;

	private double m_responseTimeAvg;

	private double m_requestPackageAvg;

	private double m_responsePackageAvg;

	public long getAccessNumberSum() {
		return m_accessNumberSum;
	}

	public int getAppVersion() {
		return m_appVersion;
	}

	public int getCity() {
		return m_city;
	}

	public int getConnectType() {
		return m_connectType;
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

	public double getRequestPackageAvg() {
		return m_requestPackageAvg;
	}

	public double getResponsePackageAvg() {
		return m_responsePackageAvg;
	}

	public double getResponseTimeAvg() {
		return m_responseTimeAvg;
	}

	public double getSuccessRatio() {
		return m_successRatio;
	}

	public AppDataSpreadInfo setAccessNumberSum(long accessNumberSum) {
		m_accessNumberSum = accessNumberSum;
		return this;
	}

	public AppDataSpreadInfo setAppVersion(int appVersion) {
		m_appVersion = appVersion;
		return this;
	}

	public AppDataSpreadInfo setCity(int city) {
		m_city = city;
		return this;
	}

	public AppDataSpreadInfo setConnectType(int connectType) {
		m_connectType = connectType;
		return this;
	}

	public AppDataSpreadInfo setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AppDataSpreadInfo setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AppDataSpreadInfo setPlatform(int platform) {
		m_platform = platform;
		return this;
	}

	public AppDataSpreadInfo setRequestPackageAvg(double requestPackageAvg) {
		m_requestPackageAvg = requestPackageAvg;
		return this;
	}

	public AppDataSpreadInfo setResponsePackageAvg(double responsePackageAvg) {
		m_responsePackageAvg = responsePackageAvg;
		return this;
	}

	public AppDataSpreadInfo setResponseTimeAvg(double responseTimeSum) {
		m_responseTimeAvg = responseTimeSum;
		return this;
	}

	public AppDataSpreadInfo setSuccessRatio(double successRatio) {
		m_successRatio = successRatio;
		return this;
	}

	@Override
	public String toString() {
		return "AppDataSpreadInfo [m_operator=" + m_operator + ", m_network=" + m_network + ", m_appVersion="
		      + m_appVersion + ", m_connectType=" + m_connectType + ", m_platform=" + m_platform + ", m_city=" + m_city
		      + ", m_successRatio=" + m_successRatio + ", m_accessNumberSum=" + m_accessNumberSum
		      + ", m_responseTimeAvg=" + m_responseTimeAvg + ", m_requestPackageAvg=" + m_requestPackageAvg
		      + ", m_responsePackageAvg=" + m_responsePackageAvg + "]";
	}

}
