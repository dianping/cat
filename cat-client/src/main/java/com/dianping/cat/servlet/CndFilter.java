package com.dianping.cat.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Metric;

public class CndFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			String souceIp = querySourceIp(httpServletRequest);
			String vip = queryVip(httpServletRequest);

			if (StringUtils.isNotEmpty(souceIp) && StringUtils.isNotEmpty(vip)) {
				Metric metric = Cat.getProducer().newMetric("cnd", vip + ":" + souceIp);

				metric.setStatus("C");
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		chain.doFilter(request, response);
	}

	private String querySourceIp(HttpServletRequest request) {
		return filterXForwardedForIP(request.getHeader("x-forwarded-for"));
	}

	private String queryVip(HttpServletRequest request) {
		return request.getHeader("x-cdn-for");
	}

	private String filterXForwardedForIP(String ip) {
		if (ip == null || ip.trim().length() == 0) {
			return null;
		} else {
			String[] subIps = ip.split(",");
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

	@Override
	public void destroy() {
	}

}
