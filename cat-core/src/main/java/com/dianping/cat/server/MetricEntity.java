package com.dianping.cat.server;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Constants;

public class MetricEntity {

	private String m_category;

	private String m_measure;

	private long m_timestamp;

	private Map<String, String> m_tags = new HashMap<String, String>();

	private Map<String, Object> m_fields = new HashMap<String, Object>();

	public MetricEntity(String category, String measure, String endPoint, long timestamp) {
		m_category = category;
		m_measure = measure;
		m_timestamp = timestamp;

		m_tags.put(Constants.END_POINT, endPoint);
	}

	public void addField(String field, Object value) {
		m_fields.put(field, value);
	}

	public void addFields(Map<String, Object> fields) {
		m_fields.putAll(fields);
	}

	public void addTag(String tag, String value) {
		m_tags.put(tag, value);
	}

	public void addTags(Map<String, String> tags) {
		m_tags.putAll(tags);
	}

	public String getCategory() {
		return m_category;
	}

	public String getEndPoint() {
		return m_tags.get(Constants.END_POINT);
	}

	public Map<String, Object> getFields() {
		return m_fields;
	}

	public String getMeasure() {
		return m_measure;
	}

	public Map<String, String> getTags() {
		return m_tags;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setFields(Map<String, Object> fields) {
		m_fields = fields;
	}

	public void setMeasure(String measure) {
		m_measure = measure;
	}

	public void setTags(Map<String, String> tags) {
		m_tags = tags;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
   public String toString() {
	   return "MetricEntity [m_category=" + m_category + ", m_measure=" + m_measure + ", m_timestamp=" + m_timestamp
	         + ", m_tags=" + m_tags + ", m_fields=" + m_fields + "]";
   }
}
