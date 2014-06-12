package com.dianping.cat.agent.monitor;

import java.util.Arrays;
import java.util.List;

public class EnvironmentConfig {

	public static String m_path = "/data/webapps/server.properties";

	// host.name=shop-web01.nh 黄永确认下
	// host.ip=10.1.4.61

	public String getIp() {
		return "10.1.1.1";
	}

	public String getDomain() {
		return "Cat";
	}

	public String getGroup() {
		return "Cat";
	}

	public List<String> getServers() {
		return Arrays.asList("cat.qa.dianpingoa.com");
	}
}
