package com.dianping.cat.abtest.spi.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.spi.ABTestContext;

public interface ABTestContextManager {
	public ABTestContext getContext(ABTestName testName);

	public void onRequestBegin(HttpServletRequest request, HttpServletResponse response);

	public void onRequestEnd();
}
