package com.dianping.cat.consumer.cross;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.dianping.cat.Cat;

public class IpConvertManager {

	private Map<String, String> m_hosts = new HashMap<String, String>();

	public String convertHostNameToIP(String hostName) {
		String result = m_hosts.get(hostName);

		if (result == null) {
			if (isIPAddress(hostName)) {
				result = hostName;
			} else {
				try {
					InetAddress address = InetAddress.getByName(hostName);

					result = address.getHostAddress();
				} catch (Exception e) {
					Cat.logError(e);
					result = "";
				}
			}
			m_hosts.put(hostName, result);
		}
		return result;
	}

	public boolean isIPAddress(String str) {
		Pattern pattern = Pattern
		      .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

}
