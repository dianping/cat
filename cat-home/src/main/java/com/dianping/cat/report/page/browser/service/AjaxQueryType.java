package com.dianping.cat.report.page.browser.service;

public enum AjaxQueryType {

	SUCCESS("success", "成功率 (%/5分钟)"),

	REQUEST("request", "请求数 (个/5分钟)"),

	DELAY("delay", "延时平均值 (毫秒/5分钟)");

	private String m_type;

	private String m_title;

	private AjaxQueryType(String type, String title) {
		m_type = type;
		m_title = title;
	}

	public String getType() {
		return m_type;
	}

	public String getTitle() {
		return m_title;
	}

	public static AjaxQueryType findByType(String type) {
		for (AjaxQueryType queryType : AjaxQueryType.values()) {
			if (queryType.getType().equals(type)) {
				return queryType;
			}
		}
		return REQUEST;
	}

}
