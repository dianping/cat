package com.dianping.cat.abtest.spi;

import java.util.Date;

import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;

public class ABTestEntity {
	private Case m_case;

	private Run m_run;

	private ABTestGroupStrategy m_groupStrategy;
	
	public ABTestEntity() {
		m_case = new Case();
		m_run = new Run();
		m_run.setDisabled(true);
	}

	public ABTestEntity(Case _case, Run run) {
		m_case = _case;
		m_run = run;
	}

	public String getGroupStrategyName() {
		return m_case.getGroupStrategy() != null ? m_case.getGroupStrategy() : null;
	}

	public String getGroupStrategyConfiguration() {
		return m_run.getGroupStrategyConfiguration() != null ? m_run.getGroupStrategyConfiguration() : null;
	}

	public int getId() {
		return m_case.getId();
	}

	public String getName() {
		return m_case.getName();
	}

	public Date getEndDate() {
		return m_run.getEndDate();
	}

	public Date getStartDate() {
		return m_run.getStartDate();
	}

	public boolean isEligible(Date date) {
		if (m_run.getDisabled() != null && m_run.getDisabled()) {
			return false;
		}

		Date startDate = m_run.getStartDate();
		if (startDate != null) {
			if (date.before(startDate)) {
				return false;
			}
		}

		Date endDate = m_run.getEndDate();
		if (endDate != null) {
			if (date.after(endDate)) {
				return false;
			}
		}

		return true;
	}

	public boolean isDisabled() {
		return m_run.isDisabled();
	}

	public void setDisabled(boolean disabled) {
		m_run.setDisabled(disabled);
	}

	public void setGroupStrategyName(String groupStrategy) {
		m_case.setGroupStrategy(groupStrategy);
	}

	public void setGroupStrategyConfiguration(String groupStrategyConfiguration) {
		m_run.setGroupStrategyConfiguration(groupStrategyConfiguration);
	}

	public void setId(int id) {
		m_case.setId(id);
	}

	public void setName(String name) {
		m_case.setName(name);
	}

	public ABTestGroupStrategy getGroupStrategy() {
		return m_groupStrategy;
	}

	public void setGroupStrategy(ABTestGroupStrategy groupStrategy) {
		m_groupStrategy = groupStrategy;
	}

	@Override
	public String toString() {
		return String.format("%s[id=%s, name=%s, groupStrategy=%s, configuation=%s]", getClass().getSimpleName(),
		      getId(), getName(), getGroupStrategyName(), getGroupStrategyConfiguration());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ABTestEntity other = (ABTestEntity) obj;
		if (getId() != other.getId())
			return false;
		return true;
	}

}