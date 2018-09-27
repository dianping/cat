package com.dianping.cat.report.page.business.task;

import org.unidal.lookup.annotation.Named;

@Named
public class BusinessKeyHelper {

	public final String SPLITTER = ":";

	public String getType(String key) {
		int index = key.lastIndexOf(SPLITTER);
		return key.substring(index + 1);
	}

	public String getBusinessItemId(String key) {
		int first = key.indexOf(SPLITTER);
		int last = key.lastIndexOf(SPLITTER);
		return key.substring(first + 1, last);
	}

	public String getDomain(String key) {
		int index = key.indexOf(SPLITTER);
		return key.substring(0, index);
	}

	public String generateKey(String id, String domain, String type) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(domain);
		sb.append(SPLITTER);
		sb.append(id);
		sb.append(SPLITTER);
		sb.append(type);

		return sb.toString();
	}
}
