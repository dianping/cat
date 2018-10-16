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
package com.dianping.cat.system.page.login.service;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.page.login.spi.ITokenManager;

public class TokenManager implements ITokenManager<SigninContext, Token> {
	@Inject
	private CookieManager m_cookieManager;

	@Inject
	private TokenBuilder m_tokenBuilder;

	@Override
	public Token getToken(SigninContext ctx, String name) {
		String value = m_cookieManager.getCookie(ctx, name);

		if (value != null) {
			return m_tokenBuilder.parse(ctx, value);
		} else {
			return null;
		}
	}

	@Override
	public void removeToken(SigninContext ctx, String name) {
		m_cookieManager.removeCookie(ctx, name);
	}

	@Override
	public void setToken(SigninContext ctx, Token token) {
		String name = token.getName();
		String value = m_tokenBuilder.build(ctx, token);

		m_cookieManager.setCookie(ctx, name, value);
	}
}
