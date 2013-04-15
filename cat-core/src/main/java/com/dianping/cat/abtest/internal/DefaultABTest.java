package com.dianping.cat.abtest.internal;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;

public class DefaultABTest implements ABTest {

	private ABTestId m_id;

	private String m_groupName;

	public DefaultABTest(ABTestId id, String groupName) {
		m_id = id;
		m_groupName = groupName;
	}

	@Override
	public ABTestId getTestId() {
		return m_id;
	}

	private String getGroupName() {
		return m_groupName;
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

	@Override
	public String toString() {
		return String.format("DefaultABTest [id=%s, groupName=%s]", m_id, m_groupName);
	}

}
