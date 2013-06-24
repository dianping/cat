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
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.abtest.ABTestManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class CatFilter implements Filter {
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		boolean top = !Cat.getManager().hasContext();

		if (top) {
			String sessionToken = getSessionIdFromCookie(req);

			Cat.setup(sessionToken);

			initCat(req);
		}

		MessageProducer cat = Cat.getProducer();
		Transaction t;

		if (top) {
			t = cat.newTransaction(getTypeName(), getOriginalUrl(request));

			logRequestClientInfo(cat, req);
			logRequestPayload(cat, req);
			prepareABTest(req, res);
		} else {
			t = cat.newTransaction(getTypeName() + ".Forward", getOriginalUrl(request));
			logRequestPayload(cat, req);
		}

		try {
			doNextFilter(request, response, chain);

			Object catStatus = request.getAttribute("cat-state");
			if (catStatus != null) {
				t.setStatus(catStatus.toString());
			} else {
				t.setStatus(Message.SUCCESS);
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
			if (top) {
				Cat.reset();
				ABTestManager.onRequestEnd();
			}
		}
	}

	protected void initCat(HttpServletRequest req) {
		String id = req.getHeader("X-Cat-Id");

		if (id != null) {
			String parentId = req.getHeader("X-Cat-Parent-Id");
			String rootId = req.getHeader("X-Cat-Root-Id");

			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

			tree.setMessageId(id);
			tree.setParentMessageId(parentId);
			tree.setRootMessageId(rootId);
		}
	}

	protected void doNextFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		chain.doFilter(request, response);
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

	protected String getTypeName() {
		return CatConstants.TYPE_URL;
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

		cat.logEvent(getTypeName(), "ClientInfo", Message.SUCCESS, sb.toString());
	}

	protected void logRequestPayload(MessageProducer cat, HttpServletRequest req) {
		StringBuilder sb = new StringBuilder(256);

		sb.append(req.getScheme().toUpperCase()).append('/');
		sb.append(req.getMethod()).append(' ').append(req.getRequestURI());

		String qs = req.getQueryString();

		if (qs != null) {
			sb.append('?').append(qs);
		}

		cat.logEvent(getTypeName(), CatConstants.NAME_PAYLOAD, Message.SUCCESS, sb.toString());
	}

	protected void prepareABTest(HttpServletRequest req, HttpServletResponse res) {
		ABTestManager.onRequestBegin(req, res);
		DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();
		String metricType = manager.getMetricType();

		if (metricType != null && metricType.length() > 0) {
			Cat.logEvent(getTypeName(), "ABTest", Message.SUCCESS, metricType);
		}
	}
}
