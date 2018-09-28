package com.dianping.cat.report.view;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DomainFilter implements Filter {

	private static final String DOMAIN = "CAT_DOMAINS";

	private static final int MAX_SIZE = 10;

	private static final String SEPARATOR = "|";

	private static final int EXPIRY = 60 * 24 * 365;

	private String buildNewCookie(String domain, String value) {
		String[] domains = value.split("\\" + SEPARATOR);
		int length = domains.length;

		for (String temp : domains) {
			if (temp.equals(domain)) {
				return null;
			}
		}
		
		if (length >= MAX_SIZE) {
			int index = value.indexOf(SEPARATOR);

			return value.substring(index + 1) + SEPARATOR + domain;
		} else {
			return value + SEPARATOR + domain;
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		httpRequest.setCharacterEncoding("utf-8");

		Cookie[] cookies = httpRequest.getCookies();
		String domain = httpRequest.getParameter("domain");
		boolean cookieExist = false;

		if (cookies != null && cookies.length > 0 && domain != null && domain.length() > 0) {
			for (Cookie cookie : cookies) {
				if (DOMAIN.equals(cookie.getName())) {
					cookieExist = true;
					String value = cookie.getValue();
					String newValue = buildNewCookie(domain, value);

					if (newValue != null) {
						Cookie c = new Cookie(DOMAIN, newValue);

						c.setMaxAge(EXPIRY);
						httpResponse.addCookie(c);
					}
				}
			}
			if (!cookieExist) {
				Cookie c = new Cookie(DOMAIN, domain);

				c.setMaxAge(EXPIRY);
				httpResponse.addCookie(c);
			}
		}

		logClientIpInfo(httpRequest);

		chain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	private void logClientIpInfo(HttpServletRequest httpRequest) {
		Cat.logEvent("UserIp", httpRequest.getRemoteAddr(), Event.SUCCESS, null);
	}

}
