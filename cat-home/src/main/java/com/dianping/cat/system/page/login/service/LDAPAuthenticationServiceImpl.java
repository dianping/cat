package com.dianping.cat.system.page.login.service;

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
import com.dianping.cat.LDAPConfigManager;
import com.dianping.cat.system.page.login.spi.ILDAPAuthenticationService;

public class LDAPAuthenticationServiceImpl implements ILDAPAuthenticationService {

	@Inject
	private LDAPConfigManager m_LDAPConfigManager;

	@Override
	public Token authenticate(String userName, String password) throws Exception {
		Token token = null;
		LdapContext ctx = null;
		String distinguishedName = null;
		String shortName = null;
		Hashtable<String, String> env = null;

		try {
			NamingEnumeration en = getInfo(userName);
			if (en == null) {
				Cat.logEvent("LoginError", "Have no NamingEnumeration.");
				return null;
			}
			if (!en.hasMoreElements()) {
				Cat.logEvent("LoginError", "Have no element.");
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
			Cat.logEvent("LoginError", userName + " doesn't exist.");
			return null;
		}

		if (shortName != null && distinguishedName != null) {
			env = new Hashtable<String, String>();

			env.put(Context.INITIAL_CONTEXT_FACTORY, m_LDAPConfigManager.getLdapFactory());
			env.put(Context.PROVIDER_URL, m_LDAPConfigManager.getLdapUrl());// LDAP server
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, distinguishedName);
			env.put(Context.SECURITY_CREDENTIALS, password);

			try {
				ctx = new InitialLdapContext(env, null);
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
	private Token getUserInfo(String cn, LdapContext ctx, String userName) {
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

					return new Token(realName, userName);
				}
			}
		} catch (Exception e) {
			Cat.logEvent("LoginError", "Exception in search():" + e);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private NamingEnumeration getInfo(String sAMAccountName) throws NamingException {
		Hashtable<String, String> solidEnv = new Hashtable<String, String>();
		solidEnv.put(Context.INITIAL_CONTEXT_FACTORY, m_LDAPConfigManager.getLdapFactory());
		solidEnv.put(Context.PROVIDER_URL, m_LDAPConfigManager.getLdapUrl());// LDAP server
		solidEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		solidEnv.put(Context.SECURITY_PRINCIPAL, "cn=" + m_LDAPConfigManager.getSolidUsername() + ","
		      + m_LDAPConfigManager.getSolidDN());
		solidEnv.put(Context.SECURITY_CREDENTIALS, m_LDAPConfigManager.getSolidPwd());
		LdapContext solidContext = new InitialLdapContext(solidEnv, null);
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration en = solidContext.search("", m_LDAPConfigManager.getLoginAttribute() + "=" + sAMAccountName,
		      constraints);

		if (en == null) {
			Cat.logEvent("LoginError", "Have no NamingEnumeration.");
		}
		if (!en.hasMoreElements()) {
			Cat.logEvent("LoginError", "Have no element.");
		}

		return en;
	}
}
