package com.dianping.cat.abtest.spi.internal.conditions;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractABTestCondition implements ABTestCondition {

	protected HttpServletRequest m_request;

	public void setRequest(HttpServletRequest request) {
		m_request = request;
	}
}
