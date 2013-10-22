package com.dianping.cat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.tuple.Pair;

import com.dianping.cat.configuration.server.entity.ConsoleConfig;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Property;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultSaxParser;
import com.dianping.cat.message.Transaction;

public class ServerConfigManager implements Initializable, LogEnabled {
	private static final long DEFAULT_HDFS_FILE_MAX_SIZE = 128 * 1024 * 1024L; // 128M

	private ServerConfig m_config;

	private Logger m_logger;

	private Set<String> m_unusedTypes = new HashSet<String>();

	private Set<String> m_unusedNames = new HashSet<String>();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getBindHost() {
		return null; // any IP address
	}

	public int getBindPort() {
		return 2280;
	}

	public String getConsoleDefaultDomain() {
		if (m_config != null) {
			return m_config.getConsole().getDefaultDomain();
		} else {
			return "Cat";
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

		return "";
	}

	public String getEmailAccount() {
		return "book.robot.dianping@gmail.com";
	}

	public String getEmailPassword() {
		return "xudgtsnoxivwclna";
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

	public String getHttpSmsApi() {
		return "";
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

	public int getLongSqlDefaultThreshold() {
		if (m_config != null) {
			LongConfig longConfig = m_config.getConsumer().getLongConfig();

			if (longConfig != null && longConfig.getDefaultUrlThreshold() != null) {
				return longConfig.getDefaultUrlThreshold();
			}
		}

		return 1000; // 1 second
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

	public String getStorageLocalBaseDir() {
		if (m_config != null) {
			StorageConfig storage = m_config.getStorage();

			return storage.getLocalBaseDir();
		} else {
			return "target/bucket";
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_unusedTypes.add("Service");
		m_unusedTypes.add("PigeonService");
		m_unusedNames.add("piegonService:heartTaskService:heartBeat");
		m_unusedNames.add("piegonService:heartTaskService:heartBeat()");
		m_unusedNames.add("pigeon:HeartBeatService:null");
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

	}

	public boolean isClientCall(String type) {
		return "PigeonCall".equals(type) || "Call".equals(type);
	}

	public boolean isHdfsOn() {
		return !m_config.getStorage().isHdfsDisabled();
	}

	public boolean isInitialized() {
		return m_config != null;
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

	public boolean isOfflineServer(String ip) {
		if (ip != null && ip.startsWith("192.")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isOnlineServer(String ip) {
		if (ip != null && ip.startsWith("10.")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSerialWrite() {
		return false;
	}

	public boolean isServerService(String type) {
		return "PigeonService".equals(type) || "Service".equals(type);
	}

	public boolean discardTransaction(Transaction t) {
		// pigeon default heartbeat is no use
		String type = t.getType();
		String name = t.getName();

		if (m_unusedTypes.contains(type) && m_unusedNames.contains(name)) {
			return true;
		}
		return false;
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

	public boolean validateDomain(String domain) {
		return !domain.equals("PhoenixAgent") && !domain.equals(Constants.FRONT_END);
	}
	
	public String getDefaultProduct(){
		return "TuanGou";
	}

}
