package com.dianping.cat.system.page.login.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.system.page.login.spi.IContext;

public class SigninContext implements IContext {
	private HttpServletRequest m_request;

	private HttpServletResponse m_response;

	public SigninContext(HttpServletRequest request, HttpServletResponse response) {
		m_request = request;
		m_response = response;
	}

	public HttpServletRequest getRequest() {
		return m_request;
	}

	public HttpServletResponse getResponse() {
		return m_response;
	}
}
