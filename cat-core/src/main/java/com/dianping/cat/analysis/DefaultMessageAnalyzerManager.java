package com.dianping.cat.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;

public class DefaultMessageAnalyzerManager extends ContainerHolder implements MessageAnalyzerManager, Initializable,
      LogEnabled {
	private static final long MINUTE = 60 * 1000L;

	private long m_duration = 60 * MINUTE;

	private long m_extraTime = 3 * MINUTE;

	private List<String> m_analyzerNames;

	private Map<Long, Map<String, List<MessageAnalyzer>>> m_analyzers = new HashMap<Long, Map<String, List<MessageAnalyzer>>>();

	protected Logger m_logger;

	@Override
	public List<MessageAnalyzer> getAnalyzer(String name, long startTime) {
		// remove last two hour analyzer
		try {
			Map<String, List<MessageAnalyzer>> temp = m_analyzers.remove(startTime - m_duration * 2);

			if (temp != null) {
				for (List<MessageAnalyzer> anlyzers : temp.values()) {
					for (MessageAnalyzer analyzer : anlyzers) {
						analyzer.destroy();
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		Map<String, List<MessageAnalyzer>> map = m_analyzers.get(startTime);

		if (map == null) {
			synchronized (m_analyzers) {
				map = m_analyzers.get(startTime);

				if (map == null) {
					map = new HashMap<String, List<MessageAnalyzer>>();
					m_analyzers.put(startTime, map);
				}
			}
		}

		List<MessageAnalyzer> analyzers = map.get(name);

		if (analyzers == null) {
			synchronized (map) {
				analyzers = map.get(name);

				if (analyzers == null) {
					analyzers = new ArrayList<MessageAnalyzer>();

					MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, name);

					analyzer.setIndex(0);
					analyzer.initialize(startTime, m_duration, m_extraTime);
					analyzers.add(analyzer);

					int count = analyzer.getAnanlyzerCount();

					for (int i = 1; i < count; i++) {
						MessageAnalyzer tempAnalyzer = lookup(MessageAnalyzer.class, name);

						tempAnalyzer.setIndex(i);
						tempAnalyzer.initialize(startTime, m_duration, m_extraTime);
						analyzers.add(tempAnalyzer);
					}
					map.put(name, analyzers);
				}
			}
		}

		return analyzers;
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
				String state = "state";
				String top = "top";

				if (state.equals(str1)) {
					return 1;
				} else if (state.equals(str2)) {
					return -1;
				}
				if (top.equals(str1)) {
					return -1;
				} else if (top.equals(str2)) {
					return 1;
				}
				return str1.compareTo(str2);
			}
		});
		
		m_analyzerNames.remove("matrix");
		m_analyzerNames.remove("dependency");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
