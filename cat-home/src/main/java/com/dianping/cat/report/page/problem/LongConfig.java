package com.dianping.cat.report.page.problem;

public class LongConfig {
	private int m_sqlThreshold = 50;

	private int m_urlThreshold = 1000;

	private int m_serviceThreshold = 50;

	private int m_cacheThreshold = 10;

	private int m_callThreshold = 50;

	public int getCacheThreshold() {
		return m_cacheThreshold;
	}

	public LongConfig setCacheThreshold(int cacheThreshold) {
		m_cacheThreshold = cacheThreshold;
		return this;
	}

	public int getCallThreshold() {
		return m_callThreshold;
	}

	public LongConfig setCallThreshold(int callThreshold) {
		m_callThreshold = callThreshold;
		return this;
	}

	public int getServiceThreshold() {
		return m_serviceThreshold;
	}

	public LongConfig setServiceThreshold(int serviceThreshold) {
		m_serviceThreshold = serviceThreshold;
		return this;
	}

	public int getSqlThreshold() {
		return m_sqlThreshold;
	}

	public LongConfig setSqlThreshold(int sqlThreshold) {
		m_sqlThreshold = sqlThreshold;
		return this;
	}

	public int getUrlThreshold() {
		return m_urlThreshold;
	}

	public LongConfig setUrlThreshold(int urlThreshold) {
		m_urlThreshold = urlThreshold;
		return this;
	}

}
