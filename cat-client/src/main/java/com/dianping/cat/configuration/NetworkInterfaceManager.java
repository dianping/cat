package com.dianping.cat.configuration;

import org.unidal.helper.Inets;

public enum NetworkInterfaceManager {
	INSTANCE;

	private NetworkInterfaceManager() {
	}

	public String getLocalHostAddress() {
		return Inets.IP4.getLocalHostAddress();
	}

	public String getLocalHostName() {
		return Inets.IP4.getLocalHostName();
	}
}
