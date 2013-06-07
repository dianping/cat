package com.dianping.cat.abtest.repository;

import java.util.Map;

import com.dianping.cat.abtest.spi.ABTestEntity;

public interface ABTestEntityRepository {
	public Map<String, ABTestEntity> getEntities();
}
