package com.dianping.cat.agent.monitor;

public class DataEntity {

	private String m_key;

	private String m_op;

	private double m_value;

	public String getKey() {
		return m_key;
	}

	public DataEntity setKey(String key) {
		m_key = key;
		return this;
	}

	public String getOp() {
		return m_op;
	}

	public DataEntity setOp(String op) {
		m_op = op;
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
