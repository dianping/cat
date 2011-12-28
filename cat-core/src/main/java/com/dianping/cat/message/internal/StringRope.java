package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.List;

public class StringRope {
	private List<String> m_parts = new ArrayList<String>();

	public StringRope append(String str) {
		if (str == null) {
			m_parts.add("null");
		} else if (str.length() > 0) {
			m_parts.add(str);
		}

		return this;
	}

	public boolean isEmpty() {
		return m_parts.isEmpty();
	}
}
