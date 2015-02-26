package com.dianping.cat.message.spi.core;

import java.util.HashSet;
import java.util.Set;

public class DomainValidator {

	private Set<String> m_valids = new HashSet<String>();

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
			m_valids.add(domain);
		}
		return result;
	}
}
