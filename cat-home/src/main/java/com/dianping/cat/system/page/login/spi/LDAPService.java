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

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Ldap;
import com.dianping.cat.message.Event;
import com.dianping.cat.system.page.login.service.Token;

public class LDAPService {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@SuppressWarnings("rawtypes")
	public Token authenticate(String userName, String password) throws Exception {
		Token token = null;
		LdapContext ctx = null;
		String distinguishedName = null;
		String shortName = null;
		Hashtable<String, String> env = null;

		try {
			NamingEnumeration en = getInfo(userName);
			if (en == null) {
				Cat.logEvent("LoginError", "HaveNoNamingEnumeration", Event.SUCCESS, null);
				return null;
			}
			if (!en.hasMoreElements()) {
				Cat.logEvent("LoginError", "HaveNoElement", Event.SUCCESS, null);
				return null;
			}
			while (en != null && en.hasMoreElements()) {
				Object obj = null;
				try {
					obj = en.nextElement();
				} catch (Exception e) {
					return null;
				}
				if (obj instanceof SearchResult) {
					SearchResult sr = (SearchResult) obj;
					Attributes attrs = sr.getAttributes();
					shortName = (String) attrs.get("cn").get();
					distinguishedName = (String) attrs.get("distinguishedName").get();
				}
			}
		} catch (NamingException ne) {
			Cat.logError(ne);
			Cat.logEvent("LoginError", userName + "NoExist", Event.SUCCESS, null);
			return null;
		}

		if (shortName != null && distinguishedName != null) {
			env = new Hashtable<String, String>();
			Ldap ldap = m_serverConfigManager.getLdap();

			env.put(Context.INITIAL_CONTEXT_FACTORY, ldap.getLdapFactory());
			env.put(Context.PROVIDER_URL, ldap.getLdapUrl());// LDAP server
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, distinguishedName);
			env.put(Context.SECURITY_CREDENTIALS, password);

			try {
				ctx = new InitialLdapContext(env, null);
			} catch (AuthenticationException e) {
				Cat.logError(e);
				throw e;
			} catch (Exception e) {
				Cat.logError(e);
				throw e;
			}
			if (ctx != null) {
				token = getUserInfo(shortName, ctx, userName);
			}
		}
		return token;
	}

	@SuppressWarnings("rawtypes")
	private NamingEnumeration getInfo(String sAMAccountName) throws NamingException {
		Hashtable<String, String> solidEnv = new Hashtable<String, String>();
		Ldap ldap = m_serverConfigManager.getLdap();

		solidEnv.put(Context.INITIAL_CONTEXT_FACTORY, ldap.getLdapFactory());
		solidEnv.put(Context.PROVIDER_URL, ldap.getLdapUrl());// LDAP server
		solidEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		solidEnv.put(Context.SECURITY_PRINCIPAL, "cn=" + ldap.getSolidUsername() + "," + ldap.getSolidDN());
		solidEnv.put(Context.SECURITY_CREDENTIALS, ldap.getSolidPassword());
		LdapContext solidContext = new InitialLdapContext(solidEnv, null);
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = solidContext.search("", ldap.getLoginAttribute() + "=" + sAMAccountName, constraints);

		if (en == null) {
			Cat.logEvent("LoginError", "HaveNoNamingEnumeration", Event.SUCCESS, null);
		}
		if (!en.hasMoreElements()) {
			Cat.logEvent("LoginError", "HaveNoElement", Event.SUCCESS, null);
		}

		return en;
	}

	@SuppressWarnings("rawtypes")
	private Token getUserInfo(String cn, LdapContext ctx, String userName) {
		String realName = null;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration en = ctx.search("", "cn=" + cn, constraints);

			if (en == null || !en.hasMoreElements()) {
				Cat.logEvent("LoginError", "HaveNoNamingEnumeration", Event.SUCCESS, null);
			}

			while (en != null && en.hasMoreElements()) {
				Object obj = en.nextElement();

				if (obj instanceof SearchResult) {
					SearchResult sr = (SearchResult) obj;
					Attributes attrs = sr.getAttributes();

					if (attrs.get("displayName") != null) {
						realName = (String) attrs.get("displayName").get();
					} else {
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
