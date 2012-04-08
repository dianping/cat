package com.dianping.cat.configuration;

import java.io.File;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.transform.DefaultXmlParser;
import com.site.helper.Files;

public class ServerConfigManager implements LogEnabled {
	private ServerConfig m_config;

	private Logger m_logger;

	public void initialize(File configFile) throws Exception {
		if (configFile != null && configFile.canRead()) {
			m_logger.info(String.format("Loading configuration file(%s) ...", configFile.getCanonicalPath()));

			String xml = Files.forIO().readFrom(configFile, "utf-8");
			ServerConfig config = new DefaultXmlParser().parse(xml);

			// do validation
			config.accept(new ServerConfigValidator());
			m_config = config;
		} else {
			if (configFile != null) {
				m_logger.warn(String.format("Configuration file(%s) not found, IGNORED.", configFile.getCanonicalPath()));
			}

			ServerConfig config = new ServerConfig();

			// do validation
			config.accept(new ServerConfigValidator());
			m_config = config;
		}
	}

	public ServerConfig getServerConfig() {
		return m_config;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
