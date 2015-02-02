package com.dianping.cat.report.view;

import java.util.Map;

public class ProblemReportHelper {

	public static String creatLinkString(String baseUrl, String classStyle, Map<String, String> params, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a ");
		sb.append("href=\"").append(baseUrl);
		for (java.util.Map.Entry<String, String> param : params.entrySet()) {
			sb.append("&").append(param.getKey()).append("=").append(param.getValue());
		}
		sb.append("\" class=\"").append(classStyle).append("\"");
		sb.append(" onclick=\"return show(this);\"").append(" >");
		if (text.trim().length() == 0) {
			sb.append("&nbsp;&nbsp");
		} else {
			sb.append(text);
		}
		sb.append("</a>");
		return sb.toString();
	}

}
