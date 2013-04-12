package com.dianping.cat.abtest.spi;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.abtest.ABTest;

public interface ABTestContext {
	public final String DEFAULT_GROUP = "default";

	public ABTestEntity getEntity();

	public void setGroupName(String groupName);

	public HttpServletRequest getHttpServletRequest();

	public ABTest getABTest();
}
