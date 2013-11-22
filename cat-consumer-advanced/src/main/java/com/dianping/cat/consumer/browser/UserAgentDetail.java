package com.dianping.cat.consumer.browser;

public class UserAgentDetail {
	private String m_browserName;

	private String m_browserVersion;

	private String m_browserComments;

	/**
	 * Constructor.
	 * 
	 * @param browserName
	 *           the name of the browser
	 * @param browserVersion
	 *           the version of the browser
	 * @param browserComments
	 *           the operating system the browser is running on
	 */
	UserAgentDetail(String browserName, String browserVersion, String browserComments) {
		this.m_browserName = browserName;
		this.m_browserVersion = browserVersion;
		this.m_browserComments = browserComments;
	}

	public String getBrowserComments() {
		return m_browserComments;
	}

	public String getBrowserName() {
		return m_browserName;
	}

	public String getBrowserVersion() {
		return m_browserVersion;
	}
}
