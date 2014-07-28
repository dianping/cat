package com.dianping.cat.report.task.alert.sender;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertEntity {

	private String m_id;

	private Date m_date;

	private String m_type;

	private String m_group;

	private String m_level;

	private String m_productline;

	private String m_metric;

	private String m_content;

	private Map<String, Object> m_paras = new HashMap<String, Object>();

	public Date getDate() {
		return m_date;
	}

	public String getContent() {
		return m_content;
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

	public String getMetric() {
		return m_metric;
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

	public void setDate(Date alertDate) {
		m_date = alertDate;
	}

	public void setContent(String content) {
		m_content = content;
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

	public void setMetric(String metricName) {
		m_metric = metricName;
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

	public class AlertEntityBuilder {

		private AlertEntity m_alertEntity = new AlertEntity();

		public AlertEntityBuilder buildDate(Date date) {
			m_alertEntity.setDate(date);
			return this;
		}

		public AlertEntityBuilder buildType(String type) {
			m_alertEntity.setType(type);
			return this;
		}

		public AlertEntityBuilder buildGroup(String group) {
			m_alertEntity.setGroup(group);
			return this;
		}

		public AlertEntityBuilder buildLevel(String level) {
			m_alertEntity.setLevel(level);
			return this;
		}

		public AlertEntityBuilder buildProductline(String productline) {
			m_alertEntity.setProductline(productline);
			return this;
		}

		public AlertEntityBuilder buildMetric(String metric) {
			m_alertEntity.setMetric(metric);
			return this;
		}

		public AlertEntityBuilder buildContent(String content) {
			m_alertEntity.setContent(content);
			return this;
		}

		public AlertEntity getAlertEntity(){
			return m_alertEntity;
		}
	}

}
