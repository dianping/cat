package com.dianping.cat.abtest.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ABTestContext {
	public final String DEFAULT_GROUP = "";

	public ABTestEntity getEntity();

	public String getGroupName();

	public HttpServletRequest getHttpServletRequest();

	public HttpServletResponse getHttpServletResponse();

	public void setGroupName(String groupName);
	
	/**
	 * @param name
	 * @param value if value is null, then remove it.
	 */
	public void setCookielet(String name, String value);
	
	public String getCookielet(String name);
}
