package com.dianping.cat.agent.monitor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.dianping.cat.Cat;

public class EnvironmentConfig {

	private static final String CONFIG_FILE = "/data/webapps/server.properties";

	private String m_ip = "10.128.120.53";

	private String m_domain = "Cat";

	public EnvironmentConfig() {
		initialize();
	}

	private void initialize() {
		try {
			Properties properties = new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(CONFIG_FILE));
			properties.load(in);

			String hostName = properties.getProperty("host.name", "Cat01.nh");
			m_domain = buildDomain(hostName);
			m_ip = properties.getProperty("host.ip", "10.10.1.1");

		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	// [**01.nh0] [**01.beta] [**-ppe01.hm]
	private String buildDomain(String hostName) {
		String domain = "";

		try {
			if (hostName.endsWith(".nh") || hostName.endsWith(".beta")) {
				domain = hostName.substring(0, hostName.lastIndexOf(".") - 2);
			} else if (hostName.endsWith("hm")) {
				domain = hostName.substring(0, hostName.lastIndexOf(".") - 6);
			} else {
				Cat.logError(new RuntimeException("Unrecognized hostName [" + hostName + "] occurs"));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return domain;
	}

	public String getIp() {
		return m_ip;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getGroup() {
		return "system-" + m_domain;
	}

	public List<String> getServers() {
		return Arrays.asList("127.0.0.1");
	}
}
