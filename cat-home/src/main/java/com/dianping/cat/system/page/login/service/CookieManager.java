package com.dianping.cat.system.page.login.service;

import javax.servlet.http.Cookie;

public class CookieManager {
	protected Cookie createCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);

		cookie.setPath("/");
		return cookie;
	}

	public String getCookie(SigninContext ctx, String name) {
		Cookie[] cookies = ctx.getRequest().getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	public void removeCookie(SigninContext ctx, String name) {
		Cookie cookie = createCookie(name, null);

		cookie.setMaxAge(0);
		ctx.getResponse().addCookie(cookie);
	}

	public void setCookie(SigninContext ctx, String name, String value) {
		Cookie cookie = createCookie(name, value);

		ctx.getResponse().addCookie(cookie);
	}
}
