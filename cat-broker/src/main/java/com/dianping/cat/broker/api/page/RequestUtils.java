package com.dianping.cat.broker.api.page;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

	public String getRemoteIp(HttpServletRequest request) {
		String ip = filterXForwardedForIP(request.getHeader("x-forwarded-for"));

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	private String filterXForwardedForIP(String ip) {
		if (ip == null || ip.trim().length() == 0) {
			return null;
		} else {
			if (ip.indexOf(',') == -1) {
				return ip;
			} else {
				String[] subIps = ip.split(",");
				// for (int i = subIps.length - 1; i >= 0; i--) {
				for (int i = 0; i < subIps.length; i++) {
					String subIp = subIps[i];
					if (subIp == null || subIp.trim().length() == 0) {
						continue;
					} else {
						subIp = subIp.trim();
						if (subIp.startsWith("192.168.") || subIp.startsWith("10.1") || subIp.startsWith("10.2")
						      || "127.0.0.1".equals(subIp)) {
							continue;
						} else {
							return subIp;
						}
					}
				}
			}
			return null;
		}
	}

}