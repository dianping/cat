package com.dianping.cat.abtest.spi;

public class ABTestEntity {
	private int m_id;

	private String m_name;

	private String m_groupStrategy;

	private String m_groupStrategyConfiguration;

	private boolean m_active;

	public String getGroupStrategy() {
		return m_groupStrategy;
	}

	public String getGroupStrategyConfiguration() {
		return m_groupStrategyConfiguration;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public boolean isActive() {
		return m_active;
	}

	public void setActive(boolean active) {
		m_active = active;
	}

	public void setGroupStrategy(String groupStrategy) {
		m_groupStrategy = groupStrategy;
	}

	public void setGroupStrategyConfiguration(String groupStrategyConfiguration) {
		m_groupStrategyConfiguration = groupStrategyConfiguration;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setName(String name) {
		m_name = name;
	}
}
