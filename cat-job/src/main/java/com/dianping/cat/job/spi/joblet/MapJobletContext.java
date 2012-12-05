package com.dianping.cat.job.spi.joblet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapJobletContext extends AbstractJobletContext {
	private Map<Object, List<Object>> m_map = new TreeMap<Object, List<Object>>();

	public Map<Object, List<Object>> getMap() {
		return m_map;
	}

	@Override
	public void write(Object key, Object value) throws IOException, InterruptedException {
		List<Object> list = m_map.get(key);

		if (list == null) {
			synchronized (m_map) {
				list = m_map.get(key);

				if (list == null) {
					list = new ArrayList<Object>();
					m_map.put(key, list);
				}
			}
		}

		synchronized (list) {
			list.add(value);
		}
	}
}