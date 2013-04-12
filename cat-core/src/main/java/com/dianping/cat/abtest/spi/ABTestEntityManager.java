package com.dianping.cat.abtest.spi;

import java.util.Map;

public interface ABTestEntityManager {
	public Map<Integer, ABTestEntity> getEntities();
}
