package com.dianping.cat.abtest.spi.internal;

import java.util.List;

import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.spi.ABTestEntity;

public interface ABTestEntityManager {
	
	public ABTestEntity getEntity(ABTestName name);

	public List<ABTestEntity> getEntityList();
}
