package com.dianping.cat;

import javax.naming.ldap.Control;

public class LDAPConfigManager {
	private static String loginAttribute = "sAMAccountName";

	private final String ldapUrl = "ldap://192.168.50.11:389/DC=dianpingoa,DC=com";

	private final String ldapBaseDN = "OU=Normal,OU=1_UserAccount,DC=dianpingoa,DC=com";

	private final String ldapFactory = "com.sun.jndi.ldap.LdapCtxFactory";

	private final String solidDN = "cn=Users,DC=dianpingoa,DC=com";

	private final String solidUsername = "lionauth";

	private final String solidPwd = "bxHxXopGJOy78Jze3LWi";

	private Control[] connCtls = null;

	public String getLoginAttribute() {
		return loginAttribute;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public String getLdapBaseDN() {
		return ldapBaseDN;
	}

	public String getLdapFactory() {
		return ldapFactory;
	}

	public String getSolidDN() {
		return solidDN;
	}

	public String getSolidUsername() {
		return solidUsername;
	}

	public String getSolidPwd() {
		return solidPwd;
	}

	public Control[] getConnCtls() {
		return connCtls;
	}
}
