package com.dianping.cat.abtest.spi.internal.conditions;

public enum ABTestComparator {

	EQUAL_INSENS(1),

	NOT_EQUAL_INSENS(2),
	
	EQUAL_SENS(3),

	NOT_EQUAL_SENS(4),

	MARCHES_INSENS(5),

	MARCHES_SENS(6),

	CONTAIN(7),

	NOT_CONTAIN(8);

	public static ABTestComparator getByValue(int value, ABTestComparator defaultComparator) {
		for (ABTestComparator comparator : ABTestComparator.values()) {
			if (comparator.m_value == value) {
				return comparator;
			}
		}

		return defaultComparator;
	}

	private int m_value;

	private ABTestComparator(int value) {
		m_value = value;
	}

}
