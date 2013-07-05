package com.dianping.cat.abtest.internal;

import java.util.Date;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.internal.ABTestContextManager;

public class DefaultABTest implements ABTest {
	private ABTestContextManager m_contextManager;

	private ABTestName m_name;

	public DefaultABTest(ABTestName name, ABTestContextManager contextManager) {
		m_contextManager = contextManager;
		m_name = name;
	}

	private String getGroupName() {
		ABTestContext ctx = m_contextManager.getContext(m_name);

		return ctx.getGroupName();
	}

	@Override
	public ABTestName getTestName() {
		return m_name;
	}

	@Override
	public boolean isActive() {
		ABTestContext ctx = m_contextManager.getContext(m_name);
		ABTestEntity entity = ctx.getEntity();

		if (entity.isDisabled()) {
			return false;
		} else {
			return entity.isEligible(new Date());
		}
	}

	@Override
	public boolean isDefaultGroup() {
		return ABTestContext.DEFAULT_GROUP.equals(getGroupName());
	}

	@Override
	public boolean isGroup(String name) {
		return name.equals(getGroupName());
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
}
