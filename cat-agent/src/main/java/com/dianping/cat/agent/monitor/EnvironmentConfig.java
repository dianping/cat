package com.dianping.cat.agent.monitor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import com.dianping.cat.configuration.NetworkInterfaceManager;

public class EnvironmentConfig implements Initializable {

	private static final String CONFIG_FILE = "/data/webapps/server.properties";

	private static final String URL_FORMAT = "http://%1$s/cat/r/monitor?op=batch&timestamp=%2$s&group=%3$s&domain=%4$s";

	private static final List<String> m_servers = Arrays.asList("localhost:2281", "10.1.110.57:8080",
	      "10.1.110.23:8080", "10.1.110.21:8080");

	private String m_ip;

	private String m_domain;

	// host.name 配置规则:
	// [${domain}01.nh0] [${domain}01.beta] [${domain}-ppe01.hm] [${domain}-sl-**] [${domain}-gp-**]
	private String buildDomain(String hostName) {
		String domain = "";

		if (hostName.endsWith(".nh") || hostName.endsWith(".beta")) {
			domain = hostName.substring(0, hostName.lastIndexOf(".") - 2);
		} else if (hostName.endsWith("hm")) {
			domain = hostName.substring(0, hostName.lastIndexOf(".") - 6);
		} else if (hostName.contains("-sl-")) {
			domain = hostName.substring(0, hostName.lastIndexOf("-sl-"));
		} else if (hostName.contains("-gp-")) {
			domain = hostName.substring(0, hostName.lastIndexOf("-gp-"));
		} else {
			throw new RuntimeException("Unrecognized hostName [" + hostName + "] occurs");
		}
		return domain;
	}

	public String buildUrl(String server) {
		String group = getGroup();
		long current = System.currentTimeMillis();

		return String.format(URL_FORMAT, server, current, group, m_domain);
	}

	public String getDomain() {
		return m_domain;
	}

	public String getGroup() {
		return "system-" + m_domain;
	}

	public String getIp() {
		return m_ip;
	}

	public List<String> getServers() {
		return m_servers;
	}

	@Override
	public void initialize() {
		try {
			Properties properties = new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(CONFIG_FILE));
			properties.load(in);

			String hostName = properties.getProperty("host.name");

			if (hostName == null) {
				hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();
			}

			m_domain = buildDomain(hostName);
			m_ip = properties.getProperty("host.ip");

			if (m_ip == null) {
				m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when init environment info ", e);
		}
	}
}
