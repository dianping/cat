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
package com.dianping.cat.system.page.permission;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.Token;
import com.dianping.cat.system.page.login.service.TokenManager;

public class PermissionFilter implements Filter {

	private static final String LOG_IN_URL = "/cat/s/login";

	private static final String LOGIN = "login";

	private static final String OP = "op";

	private static final String DEFAULT_OP = "view";

	private UserConfigManager m_userConfigManager;

	private ResourceConfigManager m_resourceConfigManager;

	private TokenManager m_tokenManager;

	private String m_errorPage;

	private String m_loginPage;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		PlexusContainer container = ContainerLoader.getDefaultContainer();
		ModuleContext ctx = new DefaultModuleContext(container);
		m_userConfigManager = ctx.lookup(UserConfigManager.class);
		m_resourceConfigManager = ctx.lookup(ResourceConfigManager.class);
		m_tokenManager = ctx.lookup(TokenManager.class);
		m_errorPage = filterConfig.getInitParameter("errorPage");
		m_loginPage = filterConfig.getInitParameter(LOGIN);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
							throws IOException,	ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpRequest.setCharacterEncoding("utf-8");

		SigninContext ctx = new SigninContext(httpRequest, httpResponse);
		String requestURI = httpRequest.getRequestURI();

		if (LOG_IN_URL.equals(requestURI)) {
			chain.doFilter(request, response);
		} else {

			String op = httpRequest.getParameter(OP);

			if (op == null) {
				op = DEFAULT_OP;
			}

			int resourceRole = m_resourceConfigManager.getRole(requestURI, op);

			if (resourceRole == ResourceConfigManager.DEFAULT_RESOURCE_ROLE) {
				chain.doFilter(request, response);
			} else {
				Token token = m_tokenManager.getToken(ctx, Token.TOKEN);

				if (token == null) {
					request.getRequestDispatcher(m_loginPage).forward(request, response);
				} else {
					int userRole = m_userConfigManager.getRole(token.getUserName());

					if (userRole >= resourceRole) {
						chain.doFilter(request, response);
					} else {
						request.getRequestDispatcher(m_errorPage).forward(request, response);
					}
				}
			}
		}
	}

	@Override
	public void destroy() {
	}

}
