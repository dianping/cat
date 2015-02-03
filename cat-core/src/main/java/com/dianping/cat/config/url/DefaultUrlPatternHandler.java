package com.dianping.cat.config.url;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.url.pattern.entity.PatternItem;

public class DefaultUrlPatternHandler implements UrlPatternHandler, LogEnabled {

	private Map<String, String> m_urlToId = new HashMap<String, String>();

	private Logger m_logger;

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
			m_logger.info(String.format("url pattern id : %s , pattern : %s", item.getName(), format));
		}
		m_urlToId = urlToId;
	}

}
