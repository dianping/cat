package com.dianping.cat.broker.api.log;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.unidal.helper.Files;

import com.dianping.cat.broker.api.log.LogManager;

public class LogServlet implements Servlet {

	private LogManager m_instance;

	@Override
	public void init(ServletConfig config) throws ServletException {
		m_instance = LogManager.getInstance();
	}

	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		InputStream in = httpReq.getInputStream();
		byte[] data = Files.forIO().readFrom(in);

		m_instance.offer(data);
	}

	@Override
	public String getServletInfo() {
		return "log-error-data";
	}

	@Override
	public void destroy() {
	}

}
