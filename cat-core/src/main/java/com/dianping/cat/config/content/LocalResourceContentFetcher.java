package com.dianping.cat.config.content;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ContentFetcher.class)
public class LocalResourceContentFetcher implements ContentFetcher, LogEnabled {
	private Logger m_logger;

	private final String PATH = "/config/";

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try {
			content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
		} catch (Exception e) {
			m_logger.warn("can't find local default config " + configName);
			Cat.logError(configName + " can't find", e);
		}
		return content;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
