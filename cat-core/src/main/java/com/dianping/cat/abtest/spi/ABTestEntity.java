package com.dianping.cat.abtest.spi;

import java.util.Date;

import com.dianping.cat.abtest.model.entity.Entity;

public class ABTestEntity {
	private Entity m_entity;

	public ABTestEntity() {
		m_entity = new Entity();
	}

	public ABTestEntity(Entity entity) {
		m_entity = entity;
	}

	public String getGroupStrategy() {
		return m_entity.getGroupStrategy().getName();
	}

	public String getGroupStrategyConfiguration() {
		return m_entity.getGroupStrategy().getConfiguration();
	}

	public int getId() {
		return m_entity.getId();
	}

	public String getName() {
		return m_entity.getName();
	}

	public boolean isEligible(Date date) {
		if (m_entity.getDisabled()) {
			return false;
		}

		Date startDate = m_entity.getStartDate();
		if (startDate != null) {
			if (date.before(startDate)) {
				return false;
			}
		}

		Date endDate = m_entity.getEndDate();
		if (endDate != null) {
			if (date.after(endDate)) {
				return false;
			}
		}

		return true;
	}

	public boolean isDisabled() {
		return m_entity.isDisabled();
	}

	public void setDisabled(boolean disabled) {
		m_entity.setDisabled(disabled);
	}

	public void setGroupStrategy(String groupStrategy) {
		m_entity.getGroupStrategy().setName(groupStrategy);
	}

	public void setGroupStrategyConfiguration(String groupStrategyConfiguration) {
		m_entity.getGroupStrategy().setConfiguration(groupStrategyConfiguration);
	}

	public void setId(int id) {
		m_entity.setId(id);
	}

	public void setName(String name) {
		m_entity.setName(name);
	}

	@Override
	public String toString() {
		return String.format("%s[id=%s, name=%s, groupStrategy=%s, configuation=%s]", getClass().getSimpleName(),
		      getId(), getName(), getGroupStrategy(), getGroupStrategyConfiguration());
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
