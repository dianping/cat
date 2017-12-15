package com.dianping.cat.config.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.server.entity.ConsoleConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultSaxParser;

public class ServerConfigManager implements LogEnabled {

	private static final long DEFAULT_HDFS_FILE_MAX_SIZE = 128 * 1024 * 1024L; // 128M

	private volatile ServerConfig m_config;

	private Logger m_logger;

	public static final String DUMP_DIR = "dump";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getConsoleDefaultDomain() {
		if (m_config != null) {
			return m_config.getConsole().getDefaultDomain().toLowerCase();
		} else {
			return Constants.CAT;
		}
	}

	public List<Pair<String, Integer>> getConsoleEndpoints() {
		if (m_config != null) {
			ConsoleConfig console = m_config.getConsole();
			String remoteServers = console.getRemoteServers();
			List<String> endpoints = Splitters.by(',').noEmptyItem().trim().split(remoteServers);
			List<Pair<String, Integer>> pairs = new ArrayList<Pair<String, Integer>>(endpoints.size());

			for (String endpoint : endpoints) {
				int pos = endpoint.indexOf(':');
				String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);
				int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos + 1)) : 2281);

				pairs.add(new Pair<String, Integer>(host, port));
			}

			return pairs;
		} else {
			return Collections.emptyList();
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

			return new File(storage.getLocalBaseDir(), id).getPath();
		} else if (id == null) {
			return "target/bucket";
		} else {
			return "target/bucket/" + id;
		}
	}

	public int getHdfsMaxStorageTime() {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getMaxHdfsStorageTime();
		} else {
			return 15;
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

	public int getLocalReportStroageTime() {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getLocalReportStorageTime();
		} else {
			return 7;
		}
	}

	public int getLogViewStroageTime() {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getLocalLogivewStorageTime();
		} else {
			return 30;
		}
	}
	
	public int getHdfsUploadThreadCount() {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getUploadThread();
		} else {
			return 5;
		}
	}

	public Map<String, Domain> getLongConfigDomains() {
		if (m_config != null) {
			LongConfig longConfig = m_config.getConsumer().getLongConfig();

			if (longConfig != null) {
				return longConfig.getDomains();
			}
		}

		return Collections.emptyMap();
	}

	public int getLongUrlDefaultThreshold() {
		if (m_config != null) {
			LongConfig longConfig = m_config.getConsumer().getLongConfig();

			if (longConfig != null && longConfig.getDefaultSqlThreshold() != null) {
				return longConfig.getDefaultSqlThreshold();
			}
		}

		return 1000; // 1 second
	}

	public ServerConfig getServerConfig() {
		return m_config;
	}

	public void initialize(File configFile) throws Exception {
		if (configFile != null && configFile.canRead()) {
			m_logger.info(String.format("Loading configuration file(%s) ...", configFile.getCanonicalPath()));

			String xml = Files.forIO().readFrom(configFile, "utf-8");
			ServerConfig config = DefaultSaxParser.parse(xml);

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
		m_logger.info("CAT server is running with hdfs," + isHdfsOn());
		m_logger.info("CAT server is running with alert," + isAlertMachine());
		m_logger.info("CAT server is running with job," + isJobMachine());
		m_logger.info(m_config.toString());
	}

	public boolean isAlertMachine() {
		if (m_config != null) {
			boolean alert = m_config.isAlertMachine();
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

			if ("10.1.6.128".equals(ip) || alert) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isCacheTransaction(String type) {
		return StringUtils.isNotEmpty(type) && type.startsWith("Cache.memcached");
	}

	public boolean isHdfsOn() {
		if (m_config != null) {
			return m_config.getHdfsMachine();
		} else {
			return false;
		}
	}

	public boolean isJobMachine() {
		if (m_config != null) {
			return m_config.isJobMachine();
		} else {
			return true;
		}
	}

	public boolean isLocalMode() {
		if (m_config != null) {
			return m_config.isLocalMode();
		} else {
			return true;
		}
	}

	public boolean isRpcClient(String type) {
		return "PigeonCall".equals(type) || "Call".equals(type);
	}

	public boolean isRpcServer(String type) {
		return "PigeonService".equals(type) || "Service".equals(type);
	}

	public boolean isSendMachine() {
		if (m_config != null) {
			return m_config.isSendMachine();
		} else {
			return false;
		}
	}

	public boolean isSQLTransaction(String type) {
		return "SQL".equals(type);
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

	public boolean validateIp(String str) {
		Pattern pattern = Pattern
		      .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

}
