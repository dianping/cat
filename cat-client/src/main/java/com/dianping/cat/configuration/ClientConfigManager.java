package com.dianping.cat.configuration;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;

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

	public ClientConfig getClientConfig() {
		return m_config;
	}

	public Domain getDomain() {
		Domain domain = null;

		if (m_config != null) {
			Map<String, Domain> domains = m_config.getDomains();

			domain = domains.isEmpty() ? null : domains.values().iterator().next();
		}

		if (domain != null) {
			return domain;
		} else {
			return new Domain("UNKNOWN").setEnabled(false);
		}
	}

	/**
	 * Return the max total message node size for the whole message, children after this limit will be split into another
	 * child message tree.
	 * 
	 * @return
	 */
	public int getMaxMessageLength() {
		if (m_config == null) {
			return 2000;
		} else {
			return getDomain().getMaxMessageSize();
		}
	}

	public String getMmapName() {
		return getPropertyValue("mmap-name", "/data/appdatas/cat/mmap");
	}

	private String getPropertyValue(String name, String defaultValue) {
		String value = defaultValue;

		if (m_config != null) {
			Property property = m_config.getProperties().get(name);

			if (property != null) {
				value = property.getText();
			}
		}

		return value;
	}

	public List<Server> getServers() {
		if (m_config == null) {
			return Collections.emptyList();
		} else {
			return m_config.getServers();
		}
	}

	public int getTaggedTransactionCacheSize() {
		return 1024;
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

				globalConfig = DefaultSaxParser.parse(xml);
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

				clientConfig = DefaultSaxParser.parse(xml);
				m_logger.info(String.format("Resource file(%s) found.", Cat.class.getResource(CAT_CLIENT_XML)));
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

	public String getServerConfigUrl() {
		if (m_config == null) {
			return null;
		} else {
			List<Server> servers = m_config.getServers();

			for (Server server : servers) {
				Integer httpPort = server.getHttpPort();

				if (httpPort == null || httpPort == 0) {
					httpPort = 8080;
				}
				return String.format("http://%s:%d/cat/s/router?domain=%s", server.getIp().trim(), httpPort, getDomain()
				      .getId());
			}
		}
		return null;
	}

	public boolean isCatEnabled() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isEnabled();
		}
	}

	public boolean isDumpLocked() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isDumpLocked();
		}
	}

	public boolean isInitialized() {
		return m_config != null;
	}
}
