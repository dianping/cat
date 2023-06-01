package com.dianping.cat.report.page.crash.display;

import java.util.Date;

public class CrashLogDetailInfo {

	private String m_appName;

	private String m_platform;

	private String m_appVersion;

	private String m_platformVersion;

	private String m_module;

	private String m_level;

	private String m_deviceBrand;

	private String m_deviceModel;

	private Date m_crashTime;
	
	private String m_dpid;

	private String m_detail;

	public String getAppName() {
		return m_appName;
	}

	public void setAppName(String appName) {
		m_appName = appName;
	}

	public String getDpid() {
		return m_dpid;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
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

	public String getModule() {
		return m_module;
	}

	public void setModule(String module) {
		m_module = module;
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

	public Date getCrashTime() {
		return m_crashTime;
	}

	public void setCrashTime(Date crashTime) {
		m_crashTime = crashTime;
	}

	public String getDetail() {
		return m_detail;
	}

	public void setDetail(String detail) {
		m_detail = detail;
	}

}
