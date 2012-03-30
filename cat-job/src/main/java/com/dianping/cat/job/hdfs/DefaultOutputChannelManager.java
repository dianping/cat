package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.server.configuration.ServerConfigManager;
import com.dianping.cat.server.configuration.entity.HdfsConfig;
import com.dianping.cat.server.configuration.entity.Property;
import com.dianping.cat.server.configuration.entity.ServerConfig;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultOutputChannelManager extends ContainerHolder implements OutputChannelManager, LogEnabled {
	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private String m_defaultBaseDir = "target/hdfs";

	private Map<String, FileSystem> m_fileSystems = new HashMap<String, FileSystem>();

	private Map<String, OutputChannel> m_channels = new HashMap<String, OutputChannel>();

	private Map<String, Integer> m_indexes = new HashMap<String, Integer>();

	private Logger m_logger;

	@Override
	public void cleanupChannels() {
		try {
			List<String> expired = new ArrayList<String>();

			for (Map.Entry<String, OutputChannel> e : m_channels.entrySet()) {
				if (e.getValue().isExpired()) {
					expired.add(e.getKey());
				}
			}

			for (String path : expired) {
				OutputChannel channel = m_channels.remove(path);

				closeChannel(channel);
			}
		} catch (Exception e) {
			m_logger.warn("Error when doing cleanup!", e);
		}
	}

	@Override
	public void closeAllChannels() {
		for (OutputChannel channel : m_channels.values()) {
			closeChannel(channel);
		}
	}

	@Override
	public void closeChannel(OutputChannel channel) {
		channel.close();
		super.release(channel);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public OutputChannel openChannel(String id, String path, boolean forceNew) throws IOException {
		String key = id + ":" + path;
		OutputChannel channel = m_channels.get(key);

		if (channel == null) {
			synchronized (m_channels) {
				channel = m_channels.get(key);

				if (channel == null) {
					channel = makeChannel(key, id, path, false);
				}
			}
		} else if (forceNew) {
			channel = makeChannel(key, id, path, true);
		}

		return channel;
	}

	private OutputChannel makeChannel(String key, String id, String path, boolean forceNew) throws IOException {
		OutputChannel channel = lookup(OutputChannel.class);
		ServerConfig config = m_manager.getServerConfig();
		HdfsConfig hdfsConfig = config.getStorage().findHdfs(id);
		FileSystem fs = m_fileSystems.get(id);
		String baseDir;

		if (hdfsConfig == null) {
			// no config found, use local HDFS
			if (fs == null) {
				fs = FileSystem.getLocal(getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir = m_defaultBaseDir + "/" + id;
		} else if (hdfsConfig.getServerUri() == null || hdfsConfig.getServerUri().length() == 0) {
			// invalid server-uri, use local HDFS instead
			if (fs == null) {
				fs = FileSystem.getLocal(getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir = m_defaultBaseDir + "/" + (hdfsConfig.getBaseDir() == null ? id : hdfsConfig.getBaseDir());
		} else {
			if (fs == null) {
				URI serverUri = URI.create(hdfsConfig.getServerUri());

				fs = FileSystem.get(serverUri, getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir = hdfsConfig.getBaseDir();
		}

		Path file;

		if (forceNew) {
			Integer index = m_indexes.get(key);

			if (index == null) {
				index = 0;
			} else {
				index++;
			}

			file = new Path(baseDir, path + (index > 0 ? "-" + index : ""));
			m_indexes.put(key, index);
		} else {
			file = new Path(baseDir, path);
		}

		FSDataOutputStream out = fs.create(file);

		channel.initialize(hdfsConfig, out);

		m_channels.put(key, channel);
		return channel;
	}

	// prepare file /etc/krb5.conf
	// prepare file /data/appdatas/cat/cat.keytab
	// prepare mapping [host] => [ip] at /etc/hosts
	// put core-site.xml at / of classpath
	// use "hdfs://dev80.hadoop:9000/user/cat" as example. Notes: host name can't
	// be an ip address
	private Configuration getHdfsConfiguration() throws IOException {
		Configuration config = new Configuration();
		Map<String, Property> properties = m_manager.getServerConfig().getStorage().getProperties();
		Property authentication = properties.get("hadoop.security.authentication");

		config.setInt("io.file.buffer.size", 8192);

		for (Property property : properties.values()) {
			config.set(property.getName(), property.getValue());
		}

		if (authentication != null && "kerberos".equals(authentication.getValue())) {
			SecurityUtil.login(config, "dfs.cat.keytab.file", "dfs.cat.kerberos.principal");
		}

		return config;
	}

	public void setDefaultBaseDir(String defaultBaseDir) {
		m_defaultBaseDir = defaultBaseDir;
	}
}
