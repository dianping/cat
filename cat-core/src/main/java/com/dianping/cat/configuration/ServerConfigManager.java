package com.dianping.cat.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.server.entity.ConsoleConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongUrl;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultXmlParser;
import com.site.helper.Files;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;

public class ServerConfigManager implements LogEnabled {
	private ServerConfig m_config;

	private List<Listener> m_listeners = new ArrayList<ServerConfigManager.Listener>();

	private Logger m_logger;

	private static final long DEFAULT_HDFS_FILE_MAX_SIZE = 128 * 1024 * 1024L; // 128M

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getConsoleDefaultDomain() {
		if (m_config != null) {
			return m_config.getConsole().getDefaultDomain();
		} else {
			return "Cat";
		}
	}

	public String getConsoleRemoteServers() {
		if (m_config != null) {
			ConsoleConfig console = m_config.getConsole();
			String remoteServers = console.getRemoteServers();

			if (remoteServers != null && remoteServers.length() > 0) {
				return remoteServers;
			}
		}

		return "127.0.0.1:2281";
	}

	public String getHdfsBaseDir(String id) {
		if (m_config != null) {
			HdfsConfig hdfsConfig = m_config.getStorage().findHdfs(id);

			if (hdfsConfig != null) {
				String baseDir = hdfsConfig.getBaseDir();

				if (baseDir != null && baseDir.trim().length() > 0) {
					return baseDir;
				}
			}
		}

		return null;
	}

	public long getHdfsFileMaxSize(String id) {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();
			HdfsConfig hdfsConfig = storage.findHdfs(id);

			return toLong(hdfsConfig == null ? null : hdfsConfig.getMaxSize(), DEFAULT_HDFS_FILE_MAX_SIZE);
		} else {
			return DEFAULT_HDFS_FILE_MAX_SIZE;
		}
	}

	public String getHdfsLocalBaseDir(String id) {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getLocalBaseDir() + "/" + id;
		} else {
			return "target/bucket/" + id;
		}
	}

	public Map<String, String> getHdfsProperties() {
		if (m_config != null) {
			Map<String, String> properties = new HashMap<String, String>();

			for (Property p : m_config.getStorage().getProperties().values()) {
				properties.put(p.getName(), p.getValue());
			}

			return properties;
		} else {
			return Collections.emptyMap();
		}
	}

	public String getHdfsServerUri(String id) {
		if (m_config != null) {
			HdfsConfig hdfsConfig = m_config.getStorage().findHdfs(id);

			if (hdfsConfig != null) {
				String serverUri = hdfsConfig.getServerUri();

				if (serverUri != null && serverUri.trim().length() > 0) {
					return serverUri;
				}
			}
		}

		return null;
	}

	public int getLongUrlDefaultThreshold() {
		if (m_config != null) {
			LongUrl longUrl = m_config.getConsumer().getLongUrl();

			if (longUrl != null && longUrl.getDefaultThreshold() != null) {
				return longUrl.getDefaultThreshold();
			}
		}

		return 1000; // 1 second
	}

	public Map<String, Domain> getLongUrlDomains() {
		if (m_config != null) {
			LongUrl longUrl = m_config.getConsumer().getLongUrl();

			if (longUrl != null) {
				return longUrl.getDomains();
			}
		}

		return Collections.emptyMap();
	}

	public ServerConfig getServerConfig() {
		return m_config;
	}

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

		if (m_config.isLocalMode()) {
			m_logger.warn("CAT server is running in LOCAL mode! No HDFS or MySQL will be accessed!");
		}

		Threads.forGroup().start(new ServerConfigReloader(configFile));
	}

	public boolean isLocalMode() {
		if (m_config != null) {
			return m_config.isLocalMode();
		} else {
			return true;
		}
	}

	private long toLong(String str, long defaultValue) {
		long value = 0;
		int len = str == null ? 0 : str.length();

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			if (Character.isDigit(ch)) {
				value = value * 10L + (ch - '0');
			} else if (ch == 'm' || ch == 'M') {
				value *= 1024 * 1024L;
			} else if (ch == 'k' || ch == 'K') {
				value *= 1024L;
			}
		}

		if (value > 0) {
			return value;
		} else {
			return defaultValue;
		}
	}

	public void onRefresh(Listener listener) {
		if (!m_listeners.contains(listener)) {
			m_listeners.add(listener);
		}
	}

	public static interface Listener {
		public void onRefresh(ServerConfigManager manager);
	}

	static class ServerConfigReloader implements Task {
		private File m_file;

		private volatile boolean m_active;

		public ServerConfigReloader(File file) {
			m_file = file;
		}

		@Override
		public void run() {
			while (isActive()) {
				try {
					if (m_file.exists()) {
						// TODO
					}
					
					Thread.sleep(2000L);
				} catch (InterruptedException e) {
					m_active = false;
				}
			}
		}

		private boolean isActive() {
			synchronized (this) {
				return m_active;
			}
		}

		@Override
		public String getName() {
			return "ServerConfigReloader";
		}

		@Override
		public void shutdown() {
			synchronized (this) {
				m_active = false;
			}
		}
	}
}
