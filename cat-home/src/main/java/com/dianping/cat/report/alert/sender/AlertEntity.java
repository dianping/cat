package com.dianping.cat.report.alert.sender;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.report.alert.AlertType;

public class AlertEntity {

	private Date m_date;

	private String m_type;

	private String m_group;

	private String m_level;

	private String m_metric;

	private String m_content;

	private Map<String, Object> m_paras = new HashMap<String, Object>();

	public String getContactGroup() {
		if (AlertType.Business.getName().equals(m_type)) {
			return String.valueOf(m_paras.get("domain"));
		} else {
			return m_group;
		}
	}

	public String getContent() {
		return m_content;
	}

	public Date getDate() {
		return m_date;
	}

	public String getDomain() {
		if (AlertType.Business.getName().equals(m_type)) {
			return String.valueOf(m_paras.get("domain"));
		} else {
			return m_group;
		}
	}

	public String getGroup() {
		return m_group;
	}

	public String getKey() {
		return m_level + ":" + m_type + ":" + m_group + ":" + m_metric;
	}

	public String getLevel() {
		return m_level;
	}

	public String getMetric() {
		return m_metric;
	}

	public Map<String, Object> getParas() {
		return m_paras;
	}

	public String getType() {
		return m_type;
	}

	public AlertEntity setContent(String content) {
		m_content = content;
		return this;
	}

	public AlertEntity setDate(Date alertDate) {
		m_date = alertDate;
		return this;
	}

	public AlertEntity setGroup(String group) {
		m_group = group;
		return this;
	}

	public AlertEntity setLevel(String level) {
		m_level = level;
		return this;
	}

	public AlertEntity setMetric(String metricName) {
		m_metric = metricName;
		return this;
	}

	public AlertEntity setParas(Map<String, Object> paras) {
		m_paras = paras;
		return this;
	}

	public AlertEntity setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public String toString() {
		return "AlertEntity [m_date=" + m_date + ", m_type=" + m_type + ", m_group=" + m_group + ", m_level=" + m_level
		      + ", m_metric=" + m_metric + "]";
	}

}
