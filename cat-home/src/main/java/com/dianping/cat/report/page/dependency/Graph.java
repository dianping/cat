package com.dianping.cat.report.page.dependency;

import java.util.HashMap;
import java.util.Map;

public class Graph {

	public Map<String, Map<String, Item>> m_dependencies = new HashMap<String, Map<String, Item>>();

	//exception count
	//totalCount url\sql\cache
	//failureCount url\sql\cache
	//pigeonCall_totalCount UserWeb\TuanGouWeb\Cache
	//Database_failure UserWeb\TuanGouWeb\Cache
	public Item findOrCreateItem(String type, String id) {
		Map<String, Item> items = m_dependencies.get(type);

		if (items == null) {
			items = new HashMap<String, Item>();
			m_dependencies.put(type, items);
		}

		Item result = items.get(id);

		if (result == null) {
			result = new Item();
			items.put(id, result);
		}

		return result;
	}

	public class Item {
		private double[] values = new double[60];

		private String title;
	}
}
