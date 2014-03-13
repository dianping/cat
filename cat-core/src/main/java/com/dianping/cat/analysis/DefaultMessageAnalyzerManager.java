package com.dianping.cat.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;

public class DefaultMessageAnalyzerManager extends ContainerHolder implements MessageAnalyzerManager, Initializable {
	private static final long MINUTE = 60 * 1000L;

	private long m_duration = 60 * MINUTE;

	private long m_extraTime = 3 * MINUTE;

	private List<String> m_analyzerNames;

	private Map<Long, Map<String, MessageAnalyzer>> m_analyzers = new HashMap<Long, Map<String, MessageAnalyzer>>();

	@Override
	public MessageAnalyzer getAnalyzer(String name, long startTime) {
		// remove last two hour analyzer
		try {
			Map<String, MessageAnalyzer> temp = m_analyzers.remove(startTime - m_duration * 3);

			if (temp != null) {
				for (MessageAnalyzer anlyzer : temp.values()) {
					anlyzer.destroy();
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		Map<String, MessageAnalyzer> map = m_analyzers.get(startTime);

		if (map == null) {
			synchronized (m_analyzers) {
				map = m_analyzers.get(startTime);

				if (map == null) {
					map = new HashMap<String, MessageAnalyzer>();
					m_analyzers.put(startTime, map);
				}
			}
		}

		MessageAnalyzer analyzer = map.get(name);

		if (analyzer == null) {
			synchronized (map) {
				analyzer = map.get(name);

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

		Collections.sort(m_analyzerNames, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				if ("state".equals(str1)) {
					return 1;
				} else if ("state".equals(str2)) {
					return -1;
				}
				return str1.compareTo(str2);
			}
		});
	}
}
