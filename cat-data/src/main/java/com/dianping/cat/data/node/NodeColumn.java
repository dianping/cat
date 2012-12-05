package com.dianping.cat.data.node;

import com.dianping.bee.engine.spi.ColumnMeta;

public enum NodeColumn implements ColumnMeta {
	StartTime(String.class), // 2012082213(for hour)

	Domain(String.class), // MobileApi

	AppIp(String.class), // 10.1.1.118

	CatIp(String.class); // 10.1.6.48

	private String m_name;

	private Class<?> m_type;

	private NodeColumn(Class<?> type) {
		m_type = type;
		m_name = name().toLowerCase();
	}

	public static NodeColumn findByName(String name) {
		for (NodeColumn column : values()) {
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}

		throw new RuntimeException(String.format("Column(%s) is not found in %s", name, NodeColumn.class.getName()));
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
