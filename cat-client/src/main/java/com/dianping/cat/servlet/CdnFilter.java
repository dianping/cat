/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Metric;

public class CdnFilter implements Filter {

	private static final String DI_LIAN = "DiLian";

	private static final String WANG_SU = "WangSu";

	private static final String TENG_XUN = "TengXun";

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
							throws IOException,	ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			String vip = queryVip(httpServletRequest);
			String sourceIp = querySourceIp(httpServletRequest);

			if (StringUtils.isNotEmpty(sourceIp) && StringUtils.isNotEmpty(vip)) {
				Metric metric = Cat.getProducer().newMetric("cdn", vip + ":" + sourceIp);

				metric.setStatus("C");
				metric.addData(String.valueOf(1));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		chain.doFilter(request, response);
	}

	private String filterXForwardedForIP(String ip) {
		if (ip == null || ip.trim().length() == 0) {
			return null;
		} else {
			String[] subIps = ip.split(",");
			int length = subIps.length;
			int index = -1;

			for (int i = 0; i < length; i++) {
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
							index = i;
							break;
						}
					} else {
						index = i;
						break;
					}
				}
			}

			if (index > -1 && index + 1 <= length) {
				return subIps[index + 1];
			} else {
				return null;
			}
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	private String querySourceIp(HttpServletRequest request) {
		return filterXForwardedForIP(request.getHeader("x-forwarded-for"));
	}

	private String queryVip(HttpServletRequest request) {
		String serverName = request.getServerName();

		if (serverName != null) {
			if (serverName.contains("s1.dpfile.com")) {
				return DI_LIAN;
			}
			if (serverName.contains("i1.dpfile.com") || serverName.contains("i3.dpfile.com")	|| serverName
									.contains("t2.dpfile.com")) {
				return DI_LIAN;
			}
			if (serverName.contains("s2.dpfile.com")) {
				return WANG_SU;
			}
			if (serverName.contains("i2.dpfile.com") || serverName.contains("t1.dpfile.com")	|| serverName
									.contains("t3.dpfile.com")) {
				return WANG_SU;
			}
			if (serverName.contains("s3.dpfile.com")) {
				return TENG_XUN;
			}
		}
		return null;
	}

}
