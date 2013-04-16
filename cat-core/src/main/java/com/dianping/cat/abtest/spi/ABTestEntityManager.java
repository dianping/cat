package com.dianping.cat.abtest.spi;

import java.util.List;

import com.dianping.cat.abtest.ABTestId;

public interface ABTestEntityManager {
	public ABTestEntity getEntity(ABTestId id);

	public List<ABTestEntity> getEntityList();
}
