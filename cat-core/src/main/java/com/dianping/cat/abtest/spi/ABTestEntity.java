package com.dianping.cat.abtest.spi;

import java.util.Date;

import com.dianping.cat.abtest.model.entity.Entity;
import com.dianping.cat.abtest.model.entity.GroupStrategy;

public class ABTestEntity {
	private Entity m_entity;

	private ABTestGroupStrategy m_groupStrategy;

	public ABTestEntity() {
		m_entity = new Entity();
		m_entity.setDisabled(true);
	}

	public ABTestEntity(Entity entity) {
		m_entity = entity;
	}

	public String getGroupStrategyName() {
		return m_entity.getGroupStrategy() != null ? m_entity.getGroupStrategy().getName() : null;
	}

	public String getGroupStrategyConfiguration() {
		return m_entity.getGroupStrategy() != null ? m_entity.getGroupStrategy().getConfiguration() : null;
	}

	public int getId() {
		return m_entity.getId();
	}

	public String getName() {
		return m_entity.getName();
	}
	
	public Date getEndDate() {
	   return m_entity.getEndDate();
   }

	public Date getStartDate() {
	   return m_entity.getStartDate();
   }

	public boolean isEligible(Date date) {
		if (m_entity.getDisabled() != null && m_entity.getDisabled()) {
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

	public void setGroupStrategyName(String groupStrategy) {
		if (m_entity.getGroupStrategy() == null) {
			m_entity.setGroupStrategy(new GroupStrategy());
		}
		m_entity.getGroupStrategy().setName(groupStrategy);
	}

	public void setGroupStrategyConfiguration(String groupStrategyConfiguration) {
		if (m_entity.getGroupStrategy() == null) {
			m_entity.setGroupStrategy(new GroupStrategy());
		}
		m_entity.getGroupStrategy().setConfiguration(groupStrategyConfiguration);
	}

	public void setId(int id) {
		m_entity.setId(id);
	}

	public void setName(String name) {
		m_entity.setName(name);
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