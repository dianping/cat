package com.dianping.cat.system.page.login.service;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.system.page.login.spi.ISessionManager;
import com.dianping.cat.system.page.login.spi.LDAPService;

public class SessionManager implements ISessionManager<Session, Token, Credential> {

	@Inject
	private LDAPService m_ldapService;

	@Override
	public Token authenticate(Credential credential) {
		String account = credential.getAccount();
		String password = credential.getPassword();
		Token token = null;

		try {
			token = m_ldapService.authenticate(account, password);
		} catch (Exception e) {
			Cat.logEvent("Login", "Login failure, uncorrected password.");
			return null;
		}

		if (token != null) {
			Cat.logEvent("Login", "Login success.");
			return token;
		}
		return null;
	}

	@Override
	public Session validate(Token token) {
		LoginMember member = new LoginMember();

		member.setUserName(token.getUserName());
		member.setRealName(token.getRealName());

		return new Session(member);
	}
}
