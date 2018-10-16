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

import com.dianping.cat.system.page.login.spi.ISigninService;

public class SigninService implements ISigninService<SigninContext, Credential, Session> {

	@Inject
	private TokenManager m_tokenManager;

	@Inject
	private SessionManager m_sessionManager;

	@Override
	public Session signin(SigninContext ctx, Credential credential) {
		Token token = m_sessionManager.authenticate(credential);

		if (token != null) {
			Session session = m_sessionManager.validate(token);

			if (session != null) {
				m_tokenManager.setToken(ctx, token);
			}
			return session;
		} else {
			return null;
		}
	}

	@Override
	public void signout(SigninContext ctx) {
		m_tokenManager.removeToken(ctx, Token.TOKEN);
	}

	@Override
	public Session validate(SigninContext ctx) {
		Token token = m_tokenManager.getToken(ctx, Token.TOKEN);

		if (token != null) {
			Session session = m_sessionManager.validate(token);

			return session;
		} else {
			return null;
		}
	}
}
