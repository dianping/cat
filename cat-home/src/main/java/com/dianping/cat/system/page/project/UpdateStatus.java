package com.dianping.cat.system.page.project;

public enum UpdateStatus {

	SUCCESS(200, "success"),

	INTERNAL_ERROR(500, "internal error");

	private int m_code;

	private String m_info;

	private UpdateStatus(int code, String info) {
		m_code = code;
		m_info = info;
	}

	public String getStatusJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\"status\":").append(m_code).append(", \"info\":\"").append(m_info).append("\"}");
		return sb.toString();
	}

}
