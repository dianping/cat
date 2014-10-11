package com.dianping.cat.agent.monitor;

public class DataEntity {

	private String m_id;

	private String m_type;

	private double m_value;

	private long m_time;

	private String m_group;

	private String m_domain;

	public String buildBatchContent() {
		StringBuilder sb = new StringBuilder();

		sb.append(getGroup()).append("\t").append(getDomain()).append("\t").append(getId()).append("\t")
		      .append(getType()).append("\t").append(getTime()).append("\t").append(getValue()).append("\n");
		return sb.toString();
	}

	public String getDomain() {
		return m_domain;
	}

	public String getGroup() {
		return m_group;
	}

	public String getId() {
		return m_id;
	}

	public long getTime() {
		return m_time;
	}

	public String getType() {
		return m_type;
	}

	public double getValue() {
		return m_value;
	}

	public DataEntity setDomain(String domain) {
		m_domain = domain;
		return this;
	}

	public DataEntity setGroup(String group) {
		m_group = group;
		return this;
	}

	public DataEntity setId(String id) {
		m_id = id;
		return this;
	}

	public DataEntity setTime(long time) {
		m_time = time;
		return this;
	}

	public DataEntity setType(String type) {
		m_type = type;
		return this;
	}

	public DataEntity setValue(double value) {
		m_value = value;
		return this;
	}

	@Override
	public String toString() {
		return "DataEntity [m_id=" + m_id + ", m_type=" + m_type + ", m_value=" + m_value + ", m_time=" + m_time
		      + ", m_group=" + m_group + ", m_domain=" + m_domain + "]";
	}

}
