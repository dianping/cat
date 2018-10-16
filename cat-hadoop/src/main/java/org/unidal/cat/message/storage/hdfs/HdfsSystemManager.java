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
package org.unidal.cat.message.storage.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;

@Named
public class HdfsSystemManager implements Initializable {
	public static final String DUMP = "dump";

	@Inject
	private ServerConfigManager m_configManager;

	private FileSystem m_fileSystem;

	private Configuration m_config;

	public String getBaseDir() {
		return m_configManager.getHdfsBaseDir(DUMP);
	}

	public Configuration getConfig() {
		return m_config;
	}

	public FileSystem getFileSystem() throws IOException {
		String serverUri = m_configManager.getHdfsServerUri(DUMP);

		if (m_fileSystem == null) {
			synchronized (this) {

				if (m_fileSystem == null) {
					URI uri = URI.create(serverUri);
					m_fileSystem = FileSystem.get(uri, m_config);
				}
			}
		}

		return m_fileSystem;
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

}
