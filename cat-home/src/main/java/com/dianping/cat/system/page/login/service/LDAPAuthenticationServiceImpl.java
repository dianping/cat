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
package com.dianping.cat.system.page.login.service;

import com.dianping.cat.Cat;
import com.dianping.cat.LDAPConfigManager;
import com.dianping.cat.system.page.login.spi.ILDAPAuthenticationService;
import org.unidal.lookup.annotation.Inject;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

/**
 * LDAPAuthenticationServiceImpl
 * 
 * @author youngphy.yang
 *
 */
public class LDAPAuthenticationServiceImpl implements ILDAPAuthenticationService {

	@Inject
	LDAPConfigManager m_LDAPConfigManager;

	/**
	 * @throws Exception
	 * @throws javax.naming.AuthenticationException
	 * @throws javax.naming.NamingException
	 * @return if authentication succeeded, return user info; otherwise, return null;
	 * @throws
	 */
	@Override
	public Token authenticate(String userName, String password) throws Exception {

		Token token = null;
		LdapContext ctx = null;
		String shortName = null;
		Hashtable<String, String> env = null;

		try {
			shortName = getShortName(userName);
		} catch (NamingException e1) {
			Cat.logEvent("LoginError", userName + " doesn't exist.");
		}

		if (shortName != null) {
			env = new Hashtable<String, String>();

			env.put(Context.INITIAL_CONTEXT_FACTORY, m_LDAPConfigManager.getLdapFactory());
			env.put(Context.PROVIDER_URL, m_LDAPConfigManager.getLdapUrl());// LDAP server
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, "cn=" + shortName + "," + m_LDAPConfigManager.getLdapBaseDN());
			env.put(Context.SECURITY_CREDENTIALS, password);

			try {
				ctx = new InitialLdapContext(env, m_LDAPConfigManager.getConnCtls());
			} catch (AuthenticationException e) {
				Cat.logEvent("LoginError", "Authentication faild: " + e.toString());
				throw e;
			} catch (Exception e) {
				Cat.logEvent("LoginError", "Something wrong while authenticating: " + e.toString());
				throw e;
			}
			if (ctx != null) {
				token = getUserInfo(shortName, ctx, userName);
			}
		}
		return token;
	}

	@SuppressWarnings("rawtypes")
	public Token getUserInfo(String cn, LdapContext ctx, String userName) {

		int memberId = 0;
		int adminId = 0;
		String realName = null;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration en = ctx.search("", "cn=" + cn, constraints);

			if (en == null || !en.hasMoreElements()) {
				Cat.logEvent("LoginError", "Have no NamingEnumeration.");
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

					return new Token(adminId, memberId, realName, userName);
				}
			}
		} catch (Exception e) {
			Cat.logEvent("LoginError", "Exception in search():" + e);
		}
		return new Token(0, null);
	}

	@SuppressWarnings("rawtypes")
	private String getShortName(String sAMAccountName) throws NamingException {
		String shortName = null;

		Hashtable<String, String> solidEnv = new Hashtable<String, String>();
		solidEnv.put(Context.INITIAL_CONTEXT_FACTORY, m_LDAPConfigManager.getLdapFactory());
		solidEnv.put(Context.PROVIDER_URL, m_LDAPConfigManager.getLdapUrl());// LDAP server
		solidEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		solidEnv.put(Context.SECURITY_PRINCIPAL, "cn=" + m_LDAPConfigManager.getSolidUsername() + ","
		      + m_LDAPConfigManager.getSolidDN());
		solidEnv.put(Context.SECURITY_CREDENTIALS, m_LDAPConfigManager.getSolidPwd());
		LdapContext solidContext = new InitialLdapContext(solidEnv, m_LDAPConfigManager.getConnCtls());
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = solidContext.search("", m_LDAPConfigManager.getLoginAttribute() + "=" + sAMAccountName,
		      constraints);

		if (en == null) {
			Cat.logEvent("LoginError", "Have no NamingEnumeration.");
			return shortName;
		}
		if (!en.hasMoreElements()) {
			Cat.logEvent("LoginError", "Have no element.");
			return shortName;
		}
		while (en != null && en.hasMoreElements()) {
			Object obj = en.nextElement();
			if (obj instanceof SearchResult) {
				SearchResult sr = (SearchResult) obj;
				// logger.debug(sr);
				Attributes attrs = sr.getAttributes();
				shortName = (String) attrs.get("cn").get();
			}
		}
		return shortName;
	}
}
