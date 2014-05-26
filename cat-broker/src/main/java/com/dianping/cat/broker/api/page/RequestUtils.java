package com.dianping.cat.broker.api.page;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

	public String filterXForwardedForIP(String ip) {
		if (ip == null || ip.trim().length() == 0) {
			return null;
		} else {
			String[] subIps = ip.split(",");
			// for (int i = subIps.length - 1; i >= 0; i--) {
			for (int i = 0; i < subIps.length; i++) {
				String subIp = subIps[i];
				if (subIp == null || subIp.trim().length() == 0) {
					continue;
				} else {
					subIp = subIp.trim();
					if (subIp.startsWith("192.168.") || subIp.startsWith("10.") || "127.0.0.1".equals(subIp)) {
						continue;
					} else if (subIp.startsWith("172.")) {
						String[] iptabs = subIp.split("\\.");
						int tab2 = Integer.parseInt(iptabs[1]);

						if (tab2 >= 16 && tab2 <= 31) {
							continue;
						} else {
							return subIp;
						}
					} else {
						return subIp;
					}
				}
			}
			return null;
		}
	}

	public String getRemoteIp(HttpServletRequest request) {
		return filterXForwardedForIP(request.getRemoteAddr() + "," + request.getHeader("x-forwarded-for"));
	}

}