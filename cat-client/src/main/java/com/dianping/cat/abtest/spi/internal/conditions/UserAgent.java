package com.dianping.cat.abtest.spi.internal.conditions;

public class UserAgent {
	private String m_os;

	private String m_browser;

	private String m_browserVersion;

	public UserAgent() {
	}

	public UserAgent(String os, String browserName, String browserVersion) {
		this.m_os = os;
		this.m_browser = browserName;
		this.m_browserVersion = browserVersion;
	}

	public String getOs() {
		return m_os;
	}

	public void setOs(String os) {
		m_os = os;
	}

	public String getBrowser() {
		return m_browser;
	}

	public void setBrowser(String browser) {
		m_browser = browser;
	}

	public String getBrowserVersion() {
		return m_browserVersion;
	}

	public void setBrowserVersion(String browserVersion) {
		m_browserVersion = browserVersion;
	}

	@Override
	public String toString() {
		return "UserAgent [m_os=" + m_os + ", m_browser=" + m_browser + ", m_browserVersion=" + m_browserVersion + "]";
	}
}
