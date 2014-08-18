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
