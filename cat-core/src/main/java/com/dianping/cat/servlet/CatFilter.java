package com.dianping.cat.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public abstract class CatFilter implements Filter {
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String sessionToken = req.getSession().getId();
		// setup for thread local data
		Cat.setup(sessionToken);
				
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL", req.getRequestURI());

		t.setStatus(Transaction.SUCCESS);
		logRequestClientInfo(cat, req);
		logRequestPayload(cat, req);

		try {
			chain.doFilter(request, response);
		} catch (ServletException e) {
			t.setStatus(e);

			throw e;
		} catch (IOException e) {
			t.setStatus(e);

			throw e;
		} catch (RuntimeException e) {
			t.setStatus(e);

			throw e;
		} finally {
			t.complete();

			// reset thread local data
			Cat.reset();
		}
	}

	protected abstract String getRequestToken();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	protected void logRequestClientInfo(MessageProducer cat, HttpServletRequest req) {
		StringBuilder sb = new StringBuilder(1024);

		sb.append("RemoteIP=").append(req.getRemoteAddr());
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

		cat.logEvent("URL", "Payload", Event.SUCCESS, sb.toString());
	}
}
