package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.ISession;

public class Session implements ISession {

	private LoginMember m_member;

	public Session(LoginMember member) {
		this.m_member = member;
	}

	public LoginMember getMember() {
		return this.m_member;
	}
}
