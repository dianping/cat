package com.dianping.cat.abtest.spi.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.model.entity.Abtest;
import com.dianping.cat.abtest.model.entity.Entity;
import com.dianping.cat.abtest.model.transform.BaseVisitor;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;
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
		try {
			InputStream in = getClass().getResourceAsStream("abtest.xml");
			Abtest abtest = DefaultSaxParser.parse(in);
			ABTestVisitor visitor = new ABTestVisitor(m_entities);

			abtest.accept(visitor);
		} catch (Exception e) {
			throw new InitializationException("Error when loading resource(abtest.xml)", e);
		}
	}

	static class ABTestVisitor extends BaseVisitor {
		private Map<Integer, ABTestEntity> m_entities;

		public ABTestVisitor(Map<Integer, ABTestEntity> entities) {
			m_entities = entities;
		}

		@Override
		public void visitEntity(Entity entity) {
			ABTestEntity abTestEntity = new ABTestEntity(entity);

			m_entities.put(abTestEntity.getId(), abTestEntity);
		}
	}
}
