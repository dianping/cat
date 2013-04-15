package com.dianping.cat.abtest.spi;

import java.util.List;
import java.util.Map;

public interface ABTestEntityManager {
	public Map<Integer, ABTestEntity> getEntities();

	public List<ABTestEntity> getEntityList();
}
