package com.dianping.cat.broker.api.app.proto;

public class ProtoData {

	protected int m_status; // -1:saved; 0:not flushed; 1:flushed

	protected long m_timestamp;

	public long getTimestamp() {
		return m_timestamp;
	}

	public boolean isFlushed() {
		return m_status == 1;
	}

	public boolean isSaved() {
		return m_status == -1;
	}

	public void setSaved() {
		m_status = -1;
	}

	public boolean notFlushed() {
		return m_status == 0;
	}

	public void setFlushed() {
		m_status = 0;
	}

	public ProtoData setTimestamp(long timestamp) {
		m_timestamp = timestamp;
		return this;
	}
}
