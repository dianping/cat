package com.dianping.cat.agent;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	private String m_groupName;

	private String m_catAddr;

	private String m_performanceDomain;

	private String m_stateDomain;

	private String m_jvmDomain;

	private String m_ethName;

	public String getGroupName() {
		return m_groupName;
	}

	public String getCatAddr() {
		return m_catAddr;
	}

	public String getPerformanceDomain() {
		return m_performanceDomain;
	}

	public String getStateDomain() {
		return m_stateDomain;
	}

	public String getJvmDomain() {
		return m_jvmDomain;
	}

	public String getEthName() {
		return m_ethName;
	}

	@Override
	public String toString() {
		return "Configuration [m_groupName=" + m_groupName + ", m_catAddr=" + m_catAddr + ", m_performanceDomain="
		      + m_performanceDomain + ", m_stateDomain=" + m_stateDomain + ", m_jvmDomain=" + m_jvmDomain
		      + ", m_ethName=" + m_ethName + "]";
	}

	public void load(String filePath) {
		try {
			Properties properties = new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(filePath));
			properties.load(in);

			m_groupName = properties.getProperty("group", "system-monitor");
			m_catAddr = properties.getProperty("cat", "unknown host");
			m_performanceDomain = properties.getProperty("performance", "performance");
			m_stateDomain = properties.getProperty("state", "state");
			m_jvmDomain = properties.getProperty("jvm", "jvm");
			m_ethName = properties.getProperty("ethernet", "eth0");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
