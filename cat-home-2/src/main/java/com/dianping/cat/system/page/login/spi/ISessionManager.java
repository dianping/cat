package com.dianping.cat.system.page.login.spi;

public interface ISessionManager<S extends ISession, T extends IToken, C extends ICredential> {
	public T authenticate(C credential);

	public S validate(T token);
}