package com.dianping.cat.agent.monitor;

public class DataEntity {

	private String m_id;

	private String m_type;

	private double m_value;

	private long m_time;

	public long getTime() {
		return m_time;
	}

	public DataEntity setTime(long time) {
		m_time = time;
		return this;
	}

	public String getId() {
		return m_id;
	}

	public DataEntity setId(String id) {
		m_id = id;
		return this;
	}

	public String getType() {
		return m_type;
	}

	public DataEntity setType(String type) {
		m_type = type;
		return this;
	}

	public double getValue() {
		return m_value;
	}

	public DataEntity setValue(double value) {
		m_value = value;
		return this;
	}

}
