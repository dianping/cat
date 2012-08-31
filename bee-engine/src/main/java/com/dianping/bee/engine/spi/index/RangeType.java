package com.dianping.bee.engine.spi.index;

public enum RangeType {
	II(true, true),

	IE(true, false),

	EI(false, true),

	EE(false, false);

	private boolean m_startInclusive;

	private boolean m_endInclusive;

	private RangeType(boolean startInclusive, boolean endInclusive) {
		m_startInclusive = startInclusive;
		m_endInclusive = endInclusive;
	}

	protected boolean isStartInclusive() {
		return m_startInclusive;
	}

	protected boolean isEndInclusive() {
		return m_endInclusive;
	}
}