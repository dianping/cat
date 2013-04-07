package com.dianping.cat.abtest.spi;

import com.dianping.cat.abtest.ABTestId;

public interface ABTestEntityManager {
	public ABTestEntity getEntity(ABTestId id);
}
