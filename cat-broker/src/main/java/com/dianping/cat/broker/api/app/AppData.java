package com.dianping.cat.broker.api.app;

public class AppData {

	protected AppDataType m_type;

	protected boolean m_flushed;

	protected long m_timestamp;

	public AppDataType getType() {
		return m_type;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public boolean isFlushed() {
		return m_flushed;
	}

	public AppData setType(AppDataType appDataType) {
		m_type = appDataType;
		return this;
	}

	public AppData setFlushed(boolean flushed) {
		m_flushed = flushed;
		return this;
	}

	public AppData setTimestamp(long timestamp) {
		m_timestamp = timestamp;
		return this;
	}

}
