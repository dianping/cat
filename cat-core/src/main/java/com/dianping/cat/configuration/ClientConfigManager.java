package com.dianping.cat.configuration;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
import com.site.helper.Files;

public class ClientConfigManager implements LogEnabled {
	private static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	private Logger m_logger;

	private ClientConfig m_config;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getBaseLogDir() {
		if (m_config == null) {
			return "target/catlog";
		} else {
			return m_config.getBaseLogDir();
		}
	}

	public Domain getFirstDomain() {
		if (m_config == null) {
			return new Domain("UNKNOWN").setEnabled(false);
		} else {
			Map<String, Domain> domains = m_config.getDomains();
			Domain firstDomain = domains.isEmpty() ? null : domains.values().iterator().next();

			return firstDomain;
		}
	}

	public List<Server> getServers() {
		if (m_config == null) {
			return Collections.emptyList();
		} else {
			return m_config.getServers();
		}
	}
	
	public ClientConfig getClientConfig(){
		return m_config;
	}

	public void initialize(File configFile) throws Exception {
		ClientConfig globalConfig = null;
		ClientConfig clientConfig = null;

		// read the global configure from local file system
		// so that OPS can:
		// - configure the cat servers to connect
		// - enable/disable Cat for specific domain(s)
		if (configFile != null) {
			if (configFile.exists()) {
				String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

				globalConfig = new DefaultDomParser().parse(xml);
				m_logger.info(String.format("Global config file(%s) found.", configFile));
			} else {
				m_logger.warn(String.format("Global config file(%s) not found, IGNORED.", configFile));
			}
		}

		// load the client configure from Java class-path
		if (clientConfig == null) {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

			if (in == null) {
				in = Cat.class.getResourceAsStream(CAT_CLIENT_XML);
			}

			if (in != null) {
				String xml = Files.forIO().readFrom(in, "utf-8");

				clientConfig = new DefaultDomParser().parse(xml);
				m_logger.info(String.format("Resource file(%s) found.", CAT_CLIENT_XML));
			} else {
				m_logger.warn(String.format("Resource file(%s) not found.", CAT_CLIENT_XML));
			}
		}

		// merge the two configures together to make it effected
		if (globalConfig != null && clientConfig != null) {
			globalConfig.accept(new ClientConfigMerger(clientConfig));
		}

		if (clientConfig != null) {
			clientConfig.accept(new ClientConfigValidator());
		}

		m_config = clientConfig;

	}

	public boolean isCatEnabled() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isEnabled();
		}
	}

	public boolean isInitialized() {
		return m_config != null;
	}
}

