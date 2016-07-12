package com.dianping.cat.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author zhangsh 50231160@qq.com
 */
public class LoginFilter implements Filter {
	private FilterConfig config;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		config = filterConfig;
	}

	@Override
	public void destroy() {
		this.config = null;

	}

	private boolean isContains(String container, String[] regx) {
		boolean result = false;

		for (int i = 0; i < regx.length; i++) {
			if (container.indexOf(regx[i]) != -1) {
				return true;
			}
		}
		return result;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponseWrapper resp = new HttpServletResponseWrapper((HttpServletResponse) response);

		String[] excludeString = StringUtils.isEmpty(config.getInitParameter("excludeRegx")) ? new String[] {} : config
		      .getInitParameter("excludeRegx").split(";");

		if (isContains(req.getRequestURI(), excludeString)) {
			chain.doFilter(request, response);
			return;
		}

		String user = (String) req.getSession().getAttribute("account");// 判断用户是否登录
		if (user == null) {
			resp.sendRedirect("/cat/s/login");
			return;
		} else {
			chain.doFilter(request, response);
			return;
		}

	}
}
