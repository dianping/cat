package com.dianping.cat.report.page.app;

public enum QueryType {

	NETWORK_SUCCESS("success", "网络成功率（%/5分钟）"),

	BUSINESS_SUCCESS("businessSuccess", "业务成功率（%/5分钟）"),

	REQUEST("request", "请求数（个/5分钟）"),

	DELAY("delay", "延时平均值（毫秒/5分钟）"),

	REQUEST_PACKAGE("requestByte", "平均发包大小（byte）"),

	RESPONSE_PACKAGE("responseByte", "平均回包大小（byte）");

	private String m_name;

	private String m_title;

	public static QueryType findByName(String name) {
		for (QueryType type : values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return REQUEST;
	}

	private QueryType(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}
}
