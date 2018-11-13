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

import com.dianping.cat.Cat;
import com.dianping.cat.CatPropertyProvider;
import com.dianping.cat.system.page.login.spi.ISessionManager;
import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionManager implements ISessionManager<Session, Token, Credential> {

	enum AuthType {
		NOP, LDAP, ADMIN_PWD
	}

	private Function<Credential, Token> tokenCreator;

	public SessionManager() {
		super();
		AuthType type = AuthType.valueOf(CatPropertyProvider.INST.getProperty("CAT_AUTH_TYPE", "ADMIN_PWD"));
		switch (type) {
		case NOP:
			tokenCreator = new Function<Credential, Token>() {
				@Override
				public Token apply(Credential credential) {
					String account = credential.getAccount();
					return new Token(account, account);
				}
			};
			break;
		case LDAP:
			final String ldapUrl = CatPropertyProvider.INST.getProperty("CAT_LDAP_URL", null);
			if (StringUtils.isBlank(ldapUrl)) {
				throw new IllegalArgumentException("required CAT_LDAP_URL");
			}
			final String userDnTpl = CatPropertyProvider.INST.getProperty("CAT_LDAP_USER_DN_TPL", null);
			if (StringUtils.isBlank(userDnTpl)) {
				throw new IllegalArgumentException("required CAT_LDAP_USER_DN_TPL");
			}
			final String userDisplayAttr = CatPropertyProvider.INST.getProperty("CAT_LDAP_USER_DISPLAY_ATTR", null);
			final Pattern pattern = Pattern.compile("\\{0}");
			final Matcher userDnTplMatcher = pattern.matcher(userDnTpl);
			final String[] attrs = userDisplayAttr == null ? null : new String[] { userDisplayAttr };
			tokenCreator = new Function<Credential, Token>() {
				@Override
				public Token apply(Credential credential) {
					final String account = credential.getAccount();
					final String pwd = credential.getPassword();
					if (StringUtils.isEmpty(account) || StringUtils.isEmpty(pwd)) {
						return null;
					}
					Hashtable<String, String> env = new Hashtable<String, String>();
					env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
					env.put(Context.PROVIDER_URL, ldapUrl);// LDAP server
					String userDn = userDnTplMatcher.replaceAll(account);
					env.put(Context.SECURITY_PRINCIPAL, pwd);
					env.put(Context.SECURITY_CREDENTIALS, pwd);
					try {
						InitialLdapContext context = new InitialLdapContext(env, null);
						final String baseDn = context.getNameInNamespace();
						if (userDn.endsWith(baseDn)) {
							userDn = userDn.substring(0, userDn.length() - baseDn.length() - 1);
						}
						String displayName = null;
						if (attrs != null) {
							final Attributes attributes = context.getAttributes(userDn, attrs);
							if (attributes.size() > 0) {
								displayName = attributes.getAll().next().get().toString();
							}
						}

						return new Token(account, displayName == null ? account : displayName);
					} catch (Exception e) {
						Cat.logError(e);
						return null;
					}
				}

			};
			break;
		case ADMIN_PWD:
			final String p = CatPropertyProvider.INST.getProperty("CAT_ADMIN_PWD", "admin");

			tokenCreator = new Function<Credential, Token>() {
				@Override
				public Token apply(Credential credential) {
					String account = credential.getAccount();

					if ("admin".equals(account) && p.equals(credential.getPassword())) {
						return new Token(account, account);
					}
					return null;
				}

			};
			break;
		}
	}

	@Override
	public Token authenticate(Credential credential) {
		return tokenCreator.apply(credential);
	}

	@Override
	public Session validate(Token token) {
		LoginMember member = new LoginMember();

		member.setUserName(token.getUserName());
		member.setRealName(token.getRealName());

		return new Session(member);
	}
}
