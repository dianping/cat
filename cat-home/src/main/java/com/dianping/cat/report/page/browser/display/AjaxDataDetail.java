package com.dianping.cat.report.page.browser.display;

import com.dianping.cat.report.page.app.service.CommandQueryEntity;

public class AjaxDataDetail {

	private int m_operator = CommandQueryEntity.DEFAULT_VALUE;

	private int m_network = CommandQueryEntity.DEFAULT_VALUE;

	private int m_appVersion = CommandQueryEntity.DEFAULT_VALUE;

	private int m_connectType = CommandQueryEntity.DEFAULT_VALUE;

	private int m_platform = CommandQueryEntity.DEFAULT_VALUE;

	private int m_city = CommandQueryEntity.DEFAULT_VALUE;

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

	public AjaxDataDetail setAccessNumberSum(long accessNumberSum) {
		m_accessNumberSum = accessNumberSum;
		return this;
	}

	public AjaxDataDetail setAppVersion(int appVersion) {
		m_appVersion = appVersion;
		return this;
	}

	public AjaxDataDetail setCity(int city) {
		m_city = city;
		return this;
	}

	public AjaxDataDetail setConnectType(int connectType) {
		m_connectType = connectType;
		return this;
	}

	public AjaxDataDetail setNetwork(int network) {
		m_network = network;
		return this;
	}

	public AjaxDataDetail setOperator(int operator) {
		m_operator = operator;
		return this;
	}

	public AjaxDataDetail setPlatform(int platform) {
		m_platform = platform;
		return this;
	}

	public AjaxDataDetail setRequestPackageAvg(double requestPackageAvg) {
		m_requestPackageAvg = requestPackageAvg;
		return this;
	}

	public AjaxDataDetail setResponsePackageAvg(double responsePackageAvg) {
		m_responsePackageAvg = responsePackageAvg;
		return this;
	}

	public AjaxDataDetail setResponseTimeAvg(double responseTimeSum) {
		m_responseTimeAvg = responseTimeSum;
		return this;
	}

	public AjaxDataDetail setSuccessRatio(double successRatio) {
		m_successRatio = successRatio;
		return this;
	}
}
