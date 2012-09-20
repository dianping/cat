package com.dianping.cat.system.page.login.service;

import com.dainping.cat.home.dal.user.DpAdminLogin;
import com.dianping.cat.system.page.login.spi.ISession;

public class Session implements ISession {
	private DpAdminLogin m_member;

	public Session(DpAdminLogin member) {
		m_member = member;
	}

	public DpAdminLogin getMember() {
		return m_member;
	}
 }
