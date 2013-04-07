package com.dianping.cat.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.abtest.ABTestManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class CatFilter implements Filter {
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String sessionToken = getSessionIdFromCookie(req);

		// setup for thread local data
		Cat.setup(sessionToken);
		ABTestManager.onRequestBegin(req);

		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction(CatConstants.TYPE_URL, getOriginalUrl(request));

		logRequestClientInfo(cat, req);
		logRequestPayload(cat, req);

		try {
			chain.doFilter(request, response);

			Object catStatus = request.getAttribute("cat-state");
			if (catStatus != null) {
				t.setStatus(catStatus.toString());
			} else {
				t.setStatus(Transaction.SUCCESS);
			}
		} catch (ServletException e) {
			cat.logError(e);
			t.setStatus(e);
			throw e;
		} catch (IOException e) {
			cat.logError(e);
			t.setStatus(e);
			throw e;
		} catch (RuntimeException e) {
			cat.logError(e);
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			cat.logError(e);
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
			Cat.reset();
			ABTestManager.onRequestEnd();
		}
	}

	protected String getOriginalUrl(ServletRequest request) {
		return ((HttpServletRequest) request).getRequestURI();
	}

	protected String getSessionIdFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	protected void logRequestClientInfo(MessageProducer cat, HttpServletRequest req) {
		StringBuilder sb = new StringBuilder(1024);
		String ip = "";
		String ipForwarded = req.getHeader("x-forwarded-for");

		if (ipForwarded == null) {
			ip = req.getRemoteAddr();
		} else {
			String ips[] = ipForwarded.split(",");

			ip = ips[ips.length - 1].trim();
		}

		sb.append("RemoteIP=").append(ip);
		sb.append("&VirtualIP=").append(req.getRemoteAddr());
		sb.append("&Server=").append(req.getServerName());
		sb.append("&Referer=").append(req.getHeader("referer"));
		sb.append("&Agent=").append(req.getHeader("user-agent"));

		cat.logEvent("URL", "ClientInfo", Message.SUCCESS, sb.toString());
	}

	protected void logRequestPayload(MessageProducer cat, HttpServletRequest req) {
		StringBuilder sb = new StringBuilder(256);

		sb.append(req.getScheme().toUpperCase()).append('/');
		sb.append(req.getMethod()).append(' ').append(req.getRequestURI());

		String qs = req.getQueryString();

		if (qs != null) {
			sb.append('?').append(qs);
		}

		cat.logEvent(CatConstants.TYPE_URL, CatConstants.NAME_PAYLOAD, Event.SUCCESS, sb.toString());
	}
}
