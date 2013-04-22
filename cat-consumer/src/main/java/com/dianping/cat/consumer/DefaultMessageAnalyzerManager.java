package com.dianping.cat.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

public class DefaultMessageAnalyzerManager extends ContainerHolder implements MessageAnalyzerManager, Initializable {
	private static final long MINUTE = 60 * 1000L;

	private long m_duration = 60 * MINUTE;

	private long m_extraTime = 3 * MINUTE;

	private List<String> m_analyzerNames;

	private Map<Long, Map<String, MessageAnalyzer>> m_map = new HashMap<Long, Map<String, MessageAnalyzer>>();

	@Override
	public MessageAnalyzer getAnalyzer(String name, long startTime) {
		Map<String, MessageAnalyzer> map = m_map.get(startTime);

		if (map == null) {
			synchronized (m_map) {
				if (map == null) {
					map = new HashMap<String, MessageAnalyzer>();
					m_map.put(startTime, map);
				}
			}
		}

		MessageAnalyzer analyzer = map.get(name);

		if (analyzer == null) {
			synchronized (map) {
				if (analyzer == null) {
					analyzer = lookup(MessageAnalyzer.class, name);
					analyzer.initialize(startTime, m_duration, m_extraTime);
					map.put(name, analyzer);
				}
			}
		}

		return analyzer;
	}

	@Override
	public List<String> getAnalyzerNames() {
		return m_analyzerNames;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, MessageAnalyzer> map = lookupMap(MessageAnalyzer.class);

		for (MessageAnalyzer analyzer : map.values()) {
			analyzer.destroy();
		}

		m_analyzerNames = new ArrayList<String>(map.keySet());
	}
}
