package com.dianping.cat.abtest.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.repository.ABTestEntityRepository;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class DefaultABTestEntityManager extends ContainerHolder implements ABTestEntityManager, Initializable {

	@Inject
	private ABTestEntityRepository m_repository;

	@Override
	public ABTestEntity getEntity(ABTestId id) {
		ABTestEntity entity = m_repository.getAllEntities().get(id.getValue());

		if (entity == null) {
			entity = new ABTestEntity();
			entity.setId(id.getValue());
			entity.setDisabled(true);

			m_repository.getAllEntities().put(id.getValue(), entity);
		}

		return entity;
	}

	public List<ABTestEntity> getEntityList() {
		List<ABTestEntity> entitiesList = new ArrayList<ABTestEntity>();
		for (ABTestEntity entity : m_repository.getAllEntities().values()) {
			entitiesList.add(entity);
		}
		return entitiesList;
	}

	@Override
	public void initialize() throws InitializationException {
		for (ABTestEntity entity : m_repository.getAllEntities().values()) {
			try {
				ABTestGroupStrategy groupStrategy = lookup(ABTestGroupStrategy.class, entity.getGroupStrategyName());

				entity.setGroupStrategy(groupStrategy);
			} catch (Exception e) {
				Cat.logError(e);
				entity.setDisabled(true);
			}
		}
	}
}