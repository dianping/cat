package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.SecurityUtil;

import com.dianping.cat.server.configuration.ServerConfigManager;
import com.dianping.cat.server.configuration.entity.HdfsConfig;
import com.dianping.cat.server.configuration.entity.Property;
import com.dianping.cat.server.configuration.entity.ServerConfig;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class FileSystemManager extends ContainerHolder {
	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private String m_defaultBaseDir = "target/hdfs";

	private Map<String, FileSystem> m_fileSystems = new HashMap<String, FileSystem>();

	public ServerConfig getServerConfig() {
		return m_manager.getServerConfig();
	}

	public FileSystem getFileSystem(String key, String id, String path, StringBuilder baseDir) throws IOException {
		ServerConfig config = m_manager.getServerConfig();
		HdfsConfig hdfsConfig = config.getStorage().findHdfs(id);
		FileSystem fs = m_fileSystems.get(id);

		if (hdfsConfig == null) {
			// no config found, use local HDFS
			if (fs == null) {
				fs = FileSystem.getLocal(getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir.append(m_defaultBaseDir).append("/").append(id);
		} else if (hdfsConfig.getServerUri() == null || hdfsConfig.getServerUri().length() == 0) {
			// invalid server-uri, use local HDFS instead
			if (fs == null) {
				fs = FileSystem.getLocal(getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir.append(m_defaultBaseDir).append("/")
			      .append(hdfsConfig.getBaseDir() == null ? id : hdfsConfig.getBaseDir());
		} else {
			if (fs == null) {
				URI serverUri = URI.create(hdfsConfig.getServerUri());

				fs = FileSystem.get(serverUri, getHdfsConfiguration());
				m_fileSystems.put(id, fs);
			}

			baseDir.append(hdfsConfig.getBaseDir());
		}

		return fs;
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
			// For MAC OS X
			// -Djava.security.krb5.realm=OX.AC.UK
			// -Djava.security.krb5.kdc=kdc0.ox.ac.uk:kdc1.ox.ac.uk

			System.setProperty("java.security.krb5.realm",
			      getValue(properties.get("java.security.krb5.realm"), "DIANPING.COM"));
			System.setProperty("java.security.krb5.kdc",
			      getValue(properties.get("java.security.krb5.kdc"), "192.168.7.80"));

			SecurityUtil.login(config, "dfs.cat.keytab.file", "dfs.cat.kerberos.principal");
		}

		return config;
	}

	private String getValue(Property property, String defaultValue) {
		if (property != null) {
			String value = property.getValue();

			return value;
		}

		return defaultValue;
	}

	public void setDefaultBaseDir(String defaultBaseDir) {
		m_defaultBaseDir = defaultBaseDir;
	}
}
