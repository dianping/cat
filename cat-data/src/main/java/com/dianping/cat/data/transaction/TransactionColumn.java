package com.dianping.cat.data.transaction;

import com.dianping.bee.engine.spi.ColumnMeta;

public enum TransactionColumn implements ColumnMeta {
	StartTime(String.class), // 20120822(for daily), 2012082213(for hour)

	Domain(String.class), // MobileApi
	
	Ip(String.class), // 127.0.0.1

	Type(String.class), // URL

	Name(String.class), // /deallist.bin

	TotalCount(Integer.class), // 2033

	FailCount(Integer.class), // 5

	SampleMessage(String.class), // MobileApi-0a0101a6-1345600834200-1

	MinDuration(Double.class), // 1

	MaxDuration(Double.class), // 1234

	AvgDuration(Double.class), // 123.56

	StdDuration(Double.class), // 236.23

	Line95(Double.class), // 123.4

	TPS(Double.class); // 12.4

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
