package com.dianping.cat.report.page.app.service;

import java.util.List;

public class FieldsInfo {
	private List<String> m_platVersions;

	private List<String> m_appVersions;

	private List<String> m_modules;

	private List<String> m_levels;

	private List<String> m_devices;

	public List<String> getDevices() {
		return m_devices;
	}

	public void setDevices(List<String> devices) {
		m_devices = devices;
	}

	public List<String> getAppVersions() {
		return m_appVersions;
	}

	public List<String> getLevels() {
		return m_levels;
	}

	public List<String> getModules() {
		return m_modules;
	}

	public List<String> getPlatVersions() {
		return m_platVersions;
	}

	public FieldsInfo setAppVersions(List<String> appVersions) {
		m_appVersions = appVersions;
		return this;
	}

	public FieldsInfo setLevels(List<String> levels) {
		m_levels = levels;
		return this;
	}

	public FieldsInfo setModules(List<String> modules) {
		m_modules = modules;
		return this;
	}

	public FieldsInfo setPlatVersions(List<String> platVersions) {
		m_platVersions = platVersions;
		return this;
	}
}
