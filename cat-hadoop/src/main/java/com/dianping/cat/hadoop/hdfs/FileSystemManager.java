/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.HarFileSystem;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;

public class FileSystemManager implements Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	private String m_defaultBaseDir;

	private Map<String, FileSystem> m_fileSystems = new HashMap<String, FileSystem>();

	private Map<String, HarConnectionPool> m_harConnPools = new HashMap<String, HarConnectionPool>();

	private Configuration m_config;

	public FileSystem getFileSystem(String id, StringBuilder basePath) throws IOException {
		String serverUri = m_configManager.getHdfsServerUri(id);
		String baseDir = m_configManager.getHdfsBaseDir(id);
		FileSystem fs = m_fileSystems.get(id);

		if (serverUri == null || !serverUri.startsWith("hdfs:")) {
			// no config found, use local HDFS
			if (fs == null) {
				fs = FileSystem.getLocal(m_config);
				m_fileSystems.put(id, fs);
			}

			basePath.append(m_defaultBaseDir).append("/");

			if (baseDir == null) {
				basePath.append(id);
			} else {
				basePath.append(baseDir);
			}
		} else {
			if (fs == null) {
				URI uri = URI.create(serverUri);
				fs = FileSystem.get(uri, m_config);
				m_fileSystems.put(id, fs);
			}

			if (baseDir == null) {
				basePath.append(id);
			} else {
				basePath.append(baseDir);
			}
			basePath.append("/");
		}

		return fs;
	}

	public HarFileSystem getHarFileSystem(String id, Date date) throws IOException {
		FileSystem fs = getFileSystem(id, new StringBuilder());
		HarConnectionPool harPool = m_harConnPools.get(id);

		if (harPool == null) {
			harPool = new HarConnectionPool(m_configManager);

			try {
				harPool.initialize();
				m_harConnPools.put(id, harPool);
			} catch (InitializationException e) {
				Cat.logError(e);
				return null;
			}
		}

		return harPool.getHarfsConnection(id, date, fs);
	}

	// prepare file /etc/krb5.conf
	// prepare mapping [host] => [ip] at /etc/hosts
	// put core-site.xml at / of classpath
	// use "hdfs://dev80.hadoop:9000/user/cat" as example. Notes: host name can't
	// be an ip address
	private Configuration getHdfsConfiguration() throws IOException {
		Configuration config = new Configuration();
		Map<String, String> properties = m_configManager.getHdfsProperties();
		String authentication = properties.get("hadoop.security.authentication");

		config.setInt("io.file.buffer.size", 8192);
		config.setInt("dfs.replication", 1);

		for (Map.Entry<String, String> property : properties.entrySet()) {
			config.set(property.getKey(), property.getValue());
		}

		if ("kerberos".equals(authentication)) {
			// For MAC OS X
			// -Djava.security.krb5.realm=OX.AC.UK
			// -Djava.security.krb5.kdc=kdc0.ox.ac.uk:kdc1.ox.ac.uk
			System.setProperty("java.security.krb5.realm",	getValue(properties, "java.security.krb5.realm", "DIANPING.COM"));
			System.setProperty("java.security.krb5.kdc", getValue(properties, "java.security.krb5.kdc", "192.168.7.80"));

			UserGroupInformation.setConfiguration(config);
		}

		return config;
	}

	private String getValue(Map<String, String> properties, String name, String defaultValue) {
		String value = properties.get(name);

		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultBaseDir = m_configManager.getHdfsLocalBaseDir("hdfs");

		if (m_configManager.isHdfsOn()) {
			try {
				m_config = getHdfsConfiguration();
				SecurityUtil.login(m_config, "dfs.cat.keytab.file", "dfs.cat.kerberos.principal");
			} catch (IOException e) {
				Cat.logError(e);
			}
		} else {
			m_config = new Configuration();
		}
	}

	public Configuration getConfig() {
		return m_config;
	}
}
