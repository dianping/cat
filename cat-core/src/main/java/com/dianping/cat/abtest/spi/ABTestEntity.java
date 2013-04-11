package com.dianping.cat.abtest.spi;

import java.util.Date;

import com.dianping.cat.abtest.model.entity.Entity;

public class ABTestEntity {

	private Entity entity;

	public ABTestEntity() {
		this.entity = new Entity();
	}

	public ABTestEntity(Entity entity) {
		this.entity = entity;
	}

	public String getGroupStrategy() {
		return entity.getGroupStrategy().getName();
	}

	public String getGroupStrategyConfiguration() {
		return entity.getGroupStrategy().getConfiguration();
	}

	public int getId() {
		return entity.getId();
	}

	public String getName() {
		return entity.getName();
	}

	public boolean isEligible(Date date) {
		if (entity.getDisabled()) {
			return false;
		}

		Date startDate = entity.getStartDate();
		if (startDate != null) {
			if (date.before(startDate)) {
				return false;
			}
		}

		Date endDate = entity.getEndDate();
		if (endDate != null) {
			if (date.after(endDate)) {
				return false;
			}
		}

		return true;
	}

	public boolean isDisabled() {
		return entity.isDisabled();
	}

	public void setDisabled(boolean disabled) {
		entity.setDisabled(disabled);
	}

	public void setGroupStrategy(String groupStrategy) {
		entity.getGroupStrategy().setName(groupStrategy);
	}

	public void setGroupStrategyConfiguration(String groupStrategyConfiguration) {
		entity.getGroupStrategy().setConfiguration(groupStrategyConfiguration);
	}

	public void setId(int id) {
		entity.setId(id);
	}

	public void setName(String name) {
		entity.setName(name);
	}

	@Override
	public String toString() {
		return "ABTestEntity [entity=" + entity + "]";
	}

}
