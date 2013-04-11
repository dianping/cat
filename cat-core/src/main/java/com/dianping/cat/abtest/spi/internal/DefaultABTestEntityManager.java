package com.dianping.cat.abtest.spi.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.sample.SampleTest.MyABTestId;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;

public class DefaultABTestEntityManager implements ABTestEntityManager, Initializable {
	private Map<Integer, ABTestEntity> m_entities = new HashMap<Integer, ABTestEntity>();

	@Override
	public ABTestEntity getEntity(ABTestId id) {
		ABTestEntity entity = m_entities.get(id.getValue());

		if (entity == null) {
			entity = new ABTestEntity();
			entity.setDisabled(true);
			m_entities.put(id.getValue(), entity);
		}

		return entity;
	}

	@Override
	public void initialize() throws InitializationException {
		// for test purpose
		ABTestEntity entity = new ABTestEntity();
		entity.setDisabled(false);
		entity.setId(MyABTestId.CASE1.getValue());
		entity.setName("abtest");
		entity.setGroupStrategy("roundrobin");
		
		m_entities.put(MyABTestId.CASE1.getValue(), entity);
	}
}
