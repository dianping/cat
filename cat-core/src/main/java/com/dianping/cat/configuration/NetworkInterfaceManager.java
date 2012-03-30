package com.dianping.cat.configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public enum NetworkInterfaceManager {
	INSTANCE;

	private InetAddress m_local;

	private NetworkInterfaceManager() {
		load();
	}

	private void load() {
		try {
			List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
			InetAddress local = null;

			for (NetworkInterface ni : nis) {
				if (ni.isUp()) {
					List<InetAddress> addresses = Collections.list(ni.getInetAddresses());

					for (InetAddress address : addresses) {
						if (address instanceof Inet4Address) {
							if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
								if (local == null) {
									local = address;
								} else if (local.isLoopbackAddress() && address.isSiteLocalAddress()) {
									local = address;
								}
							}
						}
					}
				}
			}

			m_local = local;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public String getLocalHostName() {
		return m_local.getCanonicalHostName();
	}

	public String getLocalHostAddress() {
		return m_local.getHostAddress();
	}
}
