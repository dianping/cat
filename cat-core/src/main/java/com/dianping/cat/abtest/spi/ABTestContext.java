package com.dianping.cat.abtest.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ABTestContext {
	public final String DEFAULT_GROUP = "default";

	public ABTestEntity getEntity();

	public String getGroupName();

	public void setGroupName(String groupName);

	public HttpServletRequest getHttpServletRequest();
	
	public HttpServletResponse getHttpServletResponse();
}
