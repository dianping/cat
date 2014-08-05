package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.IToken;

public class Token implements IToken {
	private int m_adminId;

	private int m_memberId;

	private String m_realName;

	private String m_userName;

	public static final String TOKEN = "ct";

	public Token(int adminId, int memberId, String realName, String userName) {
		m_adminId = adminId;
		m_memberId = memberId;
		m_realName = realName;
		m_userName = userName;
	}

	public Token(int memberId, String realName, String userName) {
		m_memberId = memberId;
		m_realName = realName;
		m_userName = userName;
	}

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

	public String getUserName() {
		return m_userName;
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
