package com.dianping.cat.abtest.spi.internal;

import java.util.List;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestEntity;

public interface ABTestEntityManager {
	
	public ABTestEntity getEntity(ABTestId id);

	public List<ABTestEntity> getEntityList();
}
