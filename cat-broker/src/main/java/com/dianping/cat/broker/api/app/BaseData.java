package com.dianping.cat.broker.api.app;

public class BaseData {

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

	public BaseData setType(AppDataType appDataType) {
		m_type = appDataType;
		return this;
	}

	public BaseData setFlushed(boolean flushed) {
		m_flushed = flushed;
		return this;
	}

	public BaseData setTimestamp(long timestamp) {
		m_timestamp = timestamp;
		return this;
	}

}
