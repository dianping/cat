package com.dianping.cat.system.page.login.spi;

public interface ITokenBuilder<C extends IContext, T extends IToken> {
	public T parse(C ctx, String str);

	public String build(C ctx, T token);
}
