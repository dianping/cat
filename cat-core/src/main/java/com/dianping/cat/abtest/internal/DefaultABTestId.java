package com.dianping.cat.abtest.internal;

import com.dianping.cat.abtest.ABTestId;

public class DefaultABTestId implements ABTestId {

	private int m_id;

	public DefaultABTestId(int id) {
		m_id = id;
	}

	@Override
	public int getValue() {
		return m_id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DefaultABTestId))
			return false;
		DefaultABTestId other = (DefaultABTestId) obj;
		if (m_id != other.m_id)
			return false;
		return true;
	}

}
