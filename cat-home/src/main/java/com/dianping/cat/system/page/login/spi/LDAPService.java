/**
 * Project: lion-service
 * 
 * File Created at 2012-8-20
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.cat.system.page.login.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Ldap;
import com.dianping.cat.message.Event;
import com.dianping.cat.system.page.login.service.Token;

public class LDAPService {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	public Token authenticate(String userName, String password) throws Exception {
		// add the default admin account
		if ("catadmin".equals(userName) && "catadmin".equals(password)) {
			return new Token(userName, userName);
		}

		Ldap ldap = m_serverConfigManager.getLdap();
		LdapContext context = null;
		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldap.getLdapUrl());// LDAP server
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userName + "@dianpingoa.com");
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			context = new InitialLdapContext(env, null);
		} catch (Exception e) {
			Cat.logError(e);
			throw e;
		}
		Token token = generateToken(userName, context);

		if (token == null) {
			token = new Token(userName, userName);
		}
		return token;
	}

	@SuppressWarnings("rawtypes")
	private Token generateToken(String userName, LdapContext context) {
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration en = context.search("", "sAMAccountName=" + userName, constraints);

			if (en == null) {
				Cat.logEvent("LoginError", userName + ":HaveNoNamingEnumeration", Event.SUCCESS, null);
			}
			if (!en.hasMoreElements()) {
				Cat.logEvent("LoginError", userName + ":HaveNoElement", Event.SUCCESS, null);
			}
			while (en.hasMoreElements()) {
				Object obj = null;
				try {
					obj = en.nextElement();
				} catch (Exception e) {
					return null;
				}
				if (obj instanceof SearchResult) {
					SearchResult sr = (SearchResult) obj;
					Attributes attrs = sr.getAttributes();
					String realName = (String) attrs.get("displayName").get();

					if (StringUtils.isEmpty(realName)) {
						realName = userName;
					}
					return new Token(realName, userName);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}
}
