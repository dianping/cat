package com.dianping.cat.report;

import java.util.concurrent.ConcurrentHashMap;

public class DomainValidator {

	private ConcurrentHashMap<String, String> m_valids = new ConcurrentHashMap<String, String>();

	public boolean validate(String domain) {
		boolean result = true;

		if (!m_valids.contains(domain)) {
			int length = domain.length();
			char c;

			for (int i = 0; i < length; i++) {
				c = domain.charAt(i);

				if (c > 126 || c < 32) {
					return false;
				}
			}
			m_valids.put(domain, domain);
		}
		return result;
	}
}
