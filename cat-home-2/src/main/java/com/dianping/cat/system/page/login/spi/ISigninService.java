package com.dianping.cat.system.page.login.spi;

public interface ISigninService<C extends IContext, T extends ICredential, S extends ISession> {
	public S signin(C ctx, T credential);

	public void signout(C ctx);

	public S validate(C ctx);
}
