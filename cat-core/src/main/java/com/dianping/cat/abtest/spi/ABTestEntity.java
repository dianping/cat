package com.dianping.cat.abtest.spi;

import java.util.Date;

public class ABTestEntity {
	private int m_id;

	private String m_name;

	private Date m_startDate;

	private Date m_endDate;

	private String m_groupStrategy;

	private String m_groupStrategyConfiguration;

	private boolean m_disabled;

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

	public boolean isEligible(Date date) {
		if (m_disabled) {
			return false;
		}

		if (m_startDate != null) {
			if (date.before(m_startDate)) {
				return false;
			}
		}

		if (m_endDate != null) {
			if (date.after(m_endDate)) {
				return false;
			}
		}

		return true;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
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
