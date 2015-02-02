package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.ICredential;

public class Credential implements ICredential {
	private String m_account;

	private String m_password;

	public Credential(String account, String password) {
		m_account = account;
		m_password = password;
	}

	public String getAccount() {
		return m_account;
	}

	public String getPassword() {
		return m_password;
	}
}
