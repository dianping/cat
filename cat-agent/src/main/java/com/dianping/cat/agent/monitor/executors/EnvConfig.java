package com.dianping.cat.agent.monitor.executors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;

public class EnvConfig implements Initializable {

	private String m_ip;

	private String m_domain;

	private String m_hostName;

	private String m_monitors;

	private String MD5_PATH = "/usr/sbin/sshd";

	private String PACKAGE_INTERFACE = "eth0";

	private String CONFIG_FILE = "/data/webapps/server.properties";

	private String CATALINA_PATH = "/data/applogs/tomcat/catalina.out";

	private List<String> TRAFFIC_INTERFACE_LIST = Arrays.asList("eth0", "lo");

	private List<String> DISK_LIST = Arrays.asList("/", "/data", "/usr", "/var");

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

	public String getCatalinaPath() {
		return CATALINA_PATH;
	}

	public String getConfig() {
		return CONFIG_FILE;
	}

	public List<String> getDiskList() {
		return DISK_LIST;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getGroup() {
		return "system-" + m_domain;
	}

	public String getHostName() {
		return m_hostName;
	}

	public String getIp() {
		return m_ip;
	}

	public String getMd5Path() {
		return MD5_PATH;
	}

	public String getMonitors() {
		return m_monitors;
	}

	public String getPackageInterface() {
		return PACKAGE_INTERFACE;
	}

	public List<String> getTrafficInterfaceList() {
		return TRAFFIC_INTERFACE_LIST;
	}

	@Override
	public void initialize() throws InitializationException {
		String agent = System.getProperty("agent", "executors");

		if ("executors".equals(agent)) {
			File configFile = new File(getConfig());
			String envMoniotors = System.getenv("MONITORS");
			String defaultMonitors = StringUtils.isEmpty(envMoniotors) ? "system" : envMoniotors;

			if (configFile.exists()) {
				loadFromConfig(defaultMonitors);
			} else {
				m_domain = "unset";
				m_monitors = defaultMonitors;
				m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				m_hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();
			}
		}
	}

	private void loadFromConfig(String defaultMonitors) {
		Properties properties = new Properties();
		InputStream in = null;

		try {
			in = new BufferedInputStream(new FileInputStream(getConfig()));
			properties.load(in);

			m_hostName = properties.getProperty("host.name");

			if (m_hostName == null) {
				m_hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();
				m_domain = "unset";
			} else {
				m_domain = buildDomain(m_hostName);
			}
			m_ip = properties.getProperty("host.ip");

			if (m_ip == null) {
				m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			}

			m_monitors = properties.getProperty("host.monitors");

			if (m_monitors == null) {
				m_monitors = defaultMonitors;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when init environment info ", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Cat.logError(e);
				}
			}
		}
	}
}
