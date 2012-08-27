package com.dianping.cat.data.transaction;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

public enum TransactionColumn implements ColumnMeta {
	StartTime(String.class), // 20120822(for daily), 2012082213(for hour)

	Domain(String.class), // MobileApi

	Type(String.class), // URL

	Name(String.class), // /deallist.bin

	TotalCount(Integer.class), // 2033

	Failures(Integer.class), // 5

	SampleMessage(String.class), // MobileApi-0a0101a6-1345600834200-1

	MinDuration(Integer.class), // 1

	MaxDuration(Integer.class), // 1234

	SumDuration(Long.class), // 123456

	Sum2Duration(Long.class), // 2364233

	Line95(Integer.class); // 123

	private String m_name;

	private Class<?> m_type;

	private TransactionColumn(Class<?> type) {
		m_type = type;
		m_name = name().toLowerCase();
	}

	public static TransactionColumn findByName(String name) {
		for (TransactionColumn column : values()) {
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}

		throw new RuntimeException(String.format("Column(%s) is not found in %s", name, TransactionColumn.class.getName()));
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public Class<?> getType() {
		return m_type;
	}
}
