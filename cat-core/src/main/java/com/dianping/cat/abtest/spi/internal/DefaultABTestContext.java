package com.dianping.cat.abtest.spi.internal;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;

public class DefaultABTestContext implements ABTestContext {
	private String m_groupName = DEFAULT_GROUP;

	private HttpServletRequest m_req;

	private ABTestEntity m_entity;

	public DefaultABTestContext(ABTestEntity entity) {
		m_entity = entity;
	}

	@Override
	public String getGroupName() {
		return m_groupName;
	}

	@Override
	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setup(HttpServletRequest req) {
		m_req = req;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return m_req;
	}

	@Override
	public ABTestEntity getEntity() {
		return m_entity;
	}
}
