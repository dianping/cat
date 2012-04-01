package com.dianping.cat.configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LocalIP {

	private static String address;

	private static final String IP_ONLINE_PREFIX = "10.1.";

	private static final String IP_TEST_PREFIX = "192.168.";

	static {
		Enumeration<?> allNetInterfaces = null;
		
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
		}
		if (allNetInterfaces != null && allNetInterfaces.hasMoreElements()) {
			InetAddress ip = null;
			
			first: while (allNetInterfaces.hasMoreElements()) {
				
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				Enumeration<?> addresses = netInterface.getInetAddresses();
				
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						String ipValue = ip.getHostAddress();
						if (ipValue.indexOf(IP_ONLINE_PREFIX) == 0) {
							address = ipValue;
							break first;
						} else if (ipValue.indexOf(IP_TEST_PREFIX) == 0) {
							address = ipValue;
						}
					}
				}
			}
		}
	}

	public static String getAddress() {
		return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
//		return address;
	}
}