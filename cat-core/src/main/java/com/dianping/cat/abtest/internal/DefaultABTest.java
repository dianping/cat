package com.dianping.cat.abtest.internal;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestContextManager;

public class DefaultABTest implements ABTest {
	private ABTestContextManager m_contextManager;

	private ABTestId m_id;

	public DefaultABTest(ABTestId id, ABTestContextManager contextManager) {
		m_contextManager = contextManager;
		m_id = id;
	}

	@Override
	public ABTestId getTestId() {
		return m_id;
	}

	private String getGroupName() {
		ABTestContext ctx = m_contextManager.getContext(m_id);

		return ctx.getGroupName();
	}

	@Override
	public boolean isDefaultGroup() {
		return ABTestContext.DEFAULT_GROUP.equals(getGroupName());
	}

	@Override
	public boolean isGroupA() {
		return "A".equals(getGroupName());
	}

	@Override
	public boolean isGroupB() {
		return "B".equals(getGroupName());
	}

	@Override
	public boolean isGroupC() {
		return "C".equals(getGroupName());
	}

	@Override
	public boolean isGroupD() {
		return "D".equals(getGroupName());
	}

	@Override
	public boolean isGroupE() {
		return "E".equals(getGroupName());
	}

	@Override
	public boolean isGroup(String name) {
		return name.equals(getGroupName());
	}
}
