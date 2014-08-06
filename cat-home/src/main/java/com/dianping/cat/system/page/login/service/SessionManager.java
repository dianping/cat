package com.dianping.cat.system.page.login.service;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.system.page.login.spi.ISessionManager;

public class SessionManager implements ISessionManager<Session, Token, Credential> {

	@Inject
	private LDAPAuthenticationServiceImpl m_LDAPService;

	@Override
	public Token authenticate(Credential credential) {
		String account = credential.getAccount();
		String password = credential.getPassword();
		Token token = null;

		try {
			token = m_LDAPService.authenticate(account, password);
		} catch (Exception e) {
			Cat.logEvent("Login", "Login failure, uncorrected password.");
			return null;
		}

		if (token != null) {
			Cat.logEvent("Login", "Login success.");
			return token;
		}

		Cat.logEvent("Login", "Login failure, uncorrected username.");
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
