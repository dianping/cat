package com.dianping.cat.abtest.spi.internal;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.internal.DefaultABTest;
import com.dianping.cat.abtest.internal.DefaultABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;

public class DefaultABTestContext implements ABTestContext {

	private final ABTestEntity m_entity;

	private HttpServletRequest m_req;

	private ABTest m_abTest = ABTest.DEFAULT;

	public DefaultABTestContext() {
		m_entity = new ABTestEntity();
	}

	public DefaultABTestContext(ABTestEntity entity) {
		m_entity = entity;
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

	@Override
	public ABTest getABTest() {
		return m_abTest;
	}

	@Override
	public void setGroupName(String groupName) {
		m_abTest = new DefaultABTest(new DefaultABTestId(m_entity.getId()), groupName);
	}

}
