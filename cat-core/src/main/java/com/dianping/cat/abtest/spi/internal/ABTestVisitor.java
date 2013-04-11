package com.dianping.cat.abtest.spi.internal;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.abtest.model.entity.Entity;
import com.dianping.cat.abtest.model.transform.BaseVisitor;
import com.dianping.cat.abtest.spi.ABTestEntity;

public class ABTestVisitor extends BaseVisitor {

	private Map<Integer, ABTestEntity> m_entities = new HashMap<Integer, ABTestEntity>();

	@Override
	public void visitEntity(Entity entity) {
		if (entity != null) {
			ABTestEntity abTestEntity = new ABTestEntity(entity);
			m_entities.put(abTestEntity.getId(), abTestEntity);
		}
	}

	public Map<Integer, ABTestEntity> getABTestEntitys() {
		return m_entities;
	}

}
