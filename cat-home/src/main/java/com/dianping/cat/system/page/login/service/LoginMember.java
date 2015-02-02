package com.dianping.cat.system.page.login.service;

public class LoginMember {

	private String m_userName;

	private String m_realName;

	public String getRealName() {
		return this.m_realName;
	}

	public String getUserName() {
		return this.m_userName;
	}

	public void setRealName(String realName) {
		this.m_realName = realName;
	}

	public void setUserName(String userName) {
		this.m_userName = userName;
	}
}
