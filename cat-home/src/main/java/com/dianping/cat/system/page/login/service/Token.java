package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.IToken;

public class Token implements IToken {
	private int m_adminId;

	private int m_memberId;

	private String m_realName;
	
	public static final String TOKEN = "ct";

	public Token(int memberId, String realName) {
		m_memberId = memberId;
		m_realName = realName;
	}

	public Token(int adminId, int memberId, String realName) {
		m_adminId = adminId;
		m_memberId = memberId;
		m_realName = realName;
	}

	public String getRealName() {
		return m_realName;
	}

	public int getAdminId() {
		return m_adminId;
	}

	public int getMemberId() {
		return m_memberId;
	}

	@Override
	public String getName() {
		return TOKEN;
	}
}
