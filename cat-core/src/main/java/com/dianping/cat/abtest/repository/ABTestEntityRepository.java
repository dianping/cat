package com.dianping.cat.abtest.repository;

import java.util.Date;
import java.util.List;

import com.dianping.cat.abtest.spi.ABTestEntity;

public interface ABTestEntityRepository {
	public List<ABTestEntity> getEntities(Date from, Date to);
}
