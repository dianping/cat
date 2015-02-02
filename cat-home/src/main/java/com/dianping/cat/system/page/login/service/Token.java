package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.IToken;

public class Token implements IToken {

	private String m_realName;

	private String m_userName;

	public static final String TOKEN = "ct";

	public Token(String realName, String userName) {
		m_realName = realName;
		m_userName = userName;
	}

	@Override
	public String getName() {
		return TOKEN;
	}

	public String getRealName() {
		return m_realName;
	}

	public String getUserName() {
		return m_userName;
	}
}
