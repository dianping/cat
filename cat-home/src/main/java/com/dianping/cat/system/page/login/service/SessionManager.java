package com.dianping.cat.system.page.login.service;

import com.dainping.cat.home.dal.user.DpAdminLogin;
import com.dainping.cat.home.dal.user.DpAdminLoginDao;
import com.dainping.cat.home.dal.user.DpAdminLoginEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.system.page.login.spi.ISessionManager;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class SessionManager implements ISessionManager<Session, Token, Credential> {
	@Inject
	private DpAdminLoginDao m_memberDao;

	@Override
	public Token authenticate(Credential credential) {
		String account = credential.getAccount();
		String password = credential.getPassword();

		try {
			DpAdminLogin member = m_memberDao.authenticate(account, password, DpAdminLoginEntity.READSET_FULL);

			return new Token(member.getLoginId());
		} catch (DalNotFoundException e) {
			// failed
		} catch (DalException e) {
			Cat.getProducer().logError(e);
		}

		return null;
	}

	@Override
	public Session validate(Token token) {
		try {
			DpAdminLogin member = m_memberDao.findByPK(token.getMemberId(), DpAdminLoginEntity.READSET_FULL);

			return new Session(member);
		} catch (DalNotFoundException e) {
			// failed
		} catch (DalException e) {
			Cat.getProducer().logError(e);
		}

		return null;
	}
}
