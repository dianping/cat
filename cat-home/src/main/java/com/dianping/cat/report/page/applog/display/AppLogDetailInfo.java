package com.dianping.cat.report.page.applog.display;

import java.util.Date;

public class AppLogDetailInfo {

	private String m_appName;

	private String m_platform;

	private String m_appVersion;

	private String m_platformVersion;

	private String m_level;

	private String m_deviceBrand;

	private String m_deviceModel;

	private Date m_logTime;

	private String m_unionId;

	private String m_detail;

	public String getAppName() {
		return m_appName;
	}

	public void setAppName(String appName) {
		m_appName = appName;
	}

	public String getPlatform() {
		return m_platform;
	}

	public void setPlatform(String platform) {
		m_platform = platform;
	}

	public String getAppVersion() {
		return m_appVersion;
	}

	public void setAppVersion(String appVersion) {
		m_appVersion = appVersion;
	}

	public String getPlatformVersion() {
		return m_platformVersion;
	}

	public void setPlatformVersion(String platformVersion) {
		m_platformVersion = platformVersion;
	}

	public String getLevel() {
		return m_level;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public String getDeviceBrand() {
		return m_deviceBrand;
	}

	public void setDeviceBrand(String deviceBrand) {
		m_deviceBrand = deviceBrand;
	}

	public String getDeviceModel() {
		return m_deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		m_deviceModel = deviceModel;
	}

	public Date getLogTime() {
		return m_logTime;
	}

	public void setLogTime(Date logTime) {
		m_logTime = logTime;
	}

	public String getUnionId() {
		return m_unionId;
	}

	public void setUnionId(String unionId) {
		m_unionId = unionId;
	}

	public String getDetail() {
		return m_detail;
	}

	public void setDetail(String detail) {
		m_detail = detail;
	}

}
