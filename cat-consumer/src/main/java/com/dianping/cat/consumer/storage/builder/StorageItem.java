package com.dianping.cat.consumer.storage.builder;

public class StorageItem {

	private String m_id;

	private String m_type;

	private String m_method;

	private String m_ip;

	private int m_threshold;

	public StorageItem(String id, String type, String method, String ip, int threshold) {
		m_id = id;
		m_type = type;
		m_method = method;
		m_ip = ip;
		m_threshold = threshold;
	}

	public String getId() {
		return m_id;
	}

	public String getIp() {
		return m_ip;
	}

	public String getMethod() {
		return m_method;
	}

	public String getReportId() {
		return m_id + "-" + m_type;
	}

	public int getThreshold() {
		return m_threshold;
	}

	public String getType() {
		return m_type;
	}

}
