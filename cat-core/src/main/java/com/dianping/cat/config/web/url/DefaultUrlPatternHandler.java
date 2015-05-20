package com.dianping.cat.config.web.url;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.web.entity.PatternItem;

public class DefaultUrlPatternHandler implements UrlPatternHandler, LogEnabled {

	private Map<String, String> m_urlToId = new HashMap<String, String>();

	protected Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String handle(String input) {
		return m_urlToId.get(input);
	}

	@Override
	public void register(Collection<PatternItem> rules) {
		Map<String, String> urlToId = new HashMap<String, String>();

		for (PatternItem item : rules) {
			String format = item.getPattern();

			urlToId.put(format, item.getName());
		}
		m_urlToId = urlToId;
	}

}
