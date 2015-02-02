package com.dianping.cat.system.page.login.service;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.page.login.spi.ITokenManager;

public class TokenManager implements ITokenManager<SigninContext, Token> {
	@Inject
	private CookieManager m_cookieManager;

	@Inject
	private TokenBuilder m_tokenBuilder;

	@Override
	public Token getToken(SigninContext ctx, String name) {
		String value = m_cookieManager.getCookie(ctx, name);

		if (value != null) {
			return m_tokenBuilder.parse(ctx, value);
		} else {
			return null;
		}
	}

	@Override
	public void removeToken(SigninContext ctx, String name) {
		m_cookieManager.removeCookie(ctx, name);
	}

	@Override
	public void setToken(SigninContext ctx, Token token) {
		String name = token.getName();
		String value = m_tokenBuilder.build(ctx, token);

		m_cookieManager.setCookie(ctx, name, value);
	}
}
