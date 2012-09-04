package com.dianping.bee.engine.spi.index;

public class Pair<S, T> {
	private S m_key;

	private T m_value;

	public Pair(S key, T value) {
		m_key = key;
		m_value = value;
	}

	public S getKey() {
		return m_key;
	}

	public T getValue() {
		return m_value;
	}
}
