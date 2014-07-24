package com.dianping.cat.report.task.alert.sender;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertEntity {

	private String m_id;

	private String m_content;

	private String m_type;

	private String m_group;

	private String m_level;

	private String m_productline;

	private String m_metricName;

	private String m_dbType;

	private Date m_alertDate;

	private Map<String, Object> m_paras = new HashMap<String, Object>();

	public Date getAlertDate() {
		return m_alertDate;
	}

	public String getContent() {
		return m_content;
	}

	public String getDbType() {
		return m_dbType;
	}

	public String getGroup() {
		return m_group;
	}

	public String getId() {
		return m_id;
	}

	public String getLevel() {
		return m_level;
	}

	public String getMetricName() {
		return m_metricName;
	}

	public Map<String, Object> getParas() {
		return m_paras;
	}

	public String getProductline() {
		return m_productline;
	}

	public String getType() {
		return m_type;
	}

	public void setAlertDate(Date alertDate) {
		m_alertDate = alertDate;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDbType(String dbType) {
		m_dbType = dbType;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public void setMetricName(String metricName) {
		m_metricName = metricName;
	}

	public void setParas(Map<String, Object> paras) {
		m_paras = paras;
	}

	public void setProductline(String productline) {
		m_productline = productline;
	}

	public void setType(String type) {
		m_type = type;
	}

}
